# stack-init

This subproject provides an Ansible playbook skeleton for bootstrapping a
project stack. It installs the `gwozdziuuu.setup` collection and currently
uses the `gwozdziuuu.setup.create_db` role against hosts defined in
`inventory/hosts.yml`. Additional roles can be added to `playbook.yml` to build
out the full infrastructure for a specific project.

## Structure

- `playbook.yml` – main playbook where roles are listed and executed against
  host groups defined in the inventory (e.g., `db`).
- `inventory/hosts.yml` – inventory file with host and connection
  details. Secrets such as passwords are stored in Ansible Vault files
  under `inventory/group_vars/<env>/`.
- `requirements.yml` – Ansible collection dependencies.
- `Makefile` – helper commands for installing dependencies, running the
  playbook and managing vault files.

## Prerequisites

- Ansible and Ansible Galaxy installed.
- Access to the target host(s) and the Ansible Vault password.

## Usage

Install the required collection:

```bash
make init
```

Check connectivity to the target host:

```bash
make ping [ENV=dev]
```

Preview the changes without applying them:

```bash
make plan [ENV=dev]
```

Run the playbook to provision stack components (such as the database):

```bash
make apply [ENV=dev]
```

The optional `ENV` variable selects the environment (defaults to
`dev`). For non-interactive runs, provide the vault password file via
`VAULT_PASS=--vault-password-file=.vault_pass.txt`.

Additional Makefile targets exist for managing Ansible Vault files such
as `vault-edit`, `vault-view`, `vault-encrypt`, and `vault-decrypt`. Run
`make help` to list all available targets.

## Manual commands

The Makefile wraps common Ansible commands. Equivalent manual commands
are:

```bash
ansible-galaxy install -r requirements.yml --force
ansible-playbook playbook.yml -i inventory/hosts.yml --check
ansible-playbook playbook.yml -i inventory/hosts.yml
```

