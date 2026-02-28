"""Entry point for Familiar inside the Android app.

Called from Kotlin via Chaquopy. This module bootstraps the Familiar
agent and Flask dashboard using Android app-private storage instead
of the default ~/.familiar directory.
"""

import os
import pathlib
import secrets


def start(data_dir: str, env_vars: dict):
    """Start the Familiar agent and dashboard.

    Args:
        data_dir: Android app-private files directory path.
        env_vars: API keys and config from Android preferences.
    """
    # Set environment variables from Android preferences
    for k, v in env_vars.items():
        os.environ[k] = str(v)

    # Hash raw owner PIN if provided (same algorithm as onboard_engine.py)
    raw_pin = os.environ.pop("FAMILIAR_OWNER_PIN_RAW", "")
    if raw_pin:
        import hashlib
        salt = secrets.token_hex(16)
        pin_hash = hashlib.pbkdf2_hmac(
            "sha256", raw_pin.encode(), salt.encode(), 100_000
        ).hex()
        os.environ["OWNER_PIN_HASH"] = f"{salt}:{pin_hash}"

    # Generate dashboard API key so Kotlin can authenticate
    api_key = secrets.token_urlsafe(32)
    os.environ["FAMILIAR_DASHBOARD_KEY"] = api_key
    data_path = pathlib.Path(data_dir)
    data_path.mkdir(parents=True, exist_ok=True)
    key_path = data_path / ".dashboard_key"
    key_path.write_text(api_key)

    # Redirect Familiar data to app-private storage
    from familiar.core import paths

    paths.set_data_root(data_path / ".familiar")
    paths.ensure_core_dirs()

    # Import and start the agent + dashboard
    from familiar.core.agent import Agent
    from familiar.dashboard.app import run_dashboard

    agent = Agent()
    run_dashboard(agent, host="127.0.0.1", port=5000, debug=False)


def stop():
    """Attempt a clean shutdown of the Flask server."""
    import signal

    os.kill(os.getpid(), signal.SIGINT)
