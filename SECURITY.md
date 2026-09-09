# Security Policy

## Supported versions

Security fixes are primarily applied to the latest development/release line.

| Version | Supported |
| --- | --- |
| Latest release | Yes |
| Development branch | Yes |
| Older releases | Best effort |

## Reporting a vulnerability

Please do **not** disclose security vulnerabilities in a public issue.

Use GitHub's private vulnerability reporting/security advisory mechanism for this repository when available. If that option is unavailable, contact the maintainer privately through the GitHub profile linked from the repository.

Include:

- A clear description of the issue.
- Reproduction steps or a minimal proof of concept.
- Affected version/commit.
- Potential impact.
- Any suggested mitigation.

Please allow reasonable time for investigation and a fix before public disclosure.

## Security principles

NovaFetch must not contain committed API keys, signing credentials, passwords, private cookies, or other secrets. Downloaded cookies and authentication material must remain user-controlled and must never be logged or uploaded by the application.
