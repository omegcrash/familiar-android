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
