GALAXY=ansible-galaxy
PLAY=ansible-playbook
INV=inventory/hosts.yml

# Environment (dev/stage/prod...). Defaults to 'dev'.
ENV ?= dev
ENV_DIR=inventory/group_vars/$(ENV)
VAULT_FILE=$(ENV_DIR)/vault.yml

# Example: VAULT_PASS="--vault-password-file=.vault_pass.txt"
VAULT_PASS ?=
ANS_VAULT=$(if $(VAULT_PASS),$(VAULT_PASS),--ask-vault-pass)
EXTRA_VARS=-e @$(VAULT_FILE)

# Host group to target (override with GROUP=..., defaults to 'db')
GROUP ?= db

help:
	@echo "Usage: make <target> [ENV=dev|stage|prod] [VAULT_PASS=--vault-password-file=.vault_pass.txt]"
	@echo "Targets: init, ping, plan, apply, vault-edit, vault-encrypt, vault-decrypt, vault-view, vault-decrypt-tmp, vault-rekey"

init:
	$(GALAXY) install -r requirements.yml --force

env-check:
	@echo ">> ENV=$(ENV)"
	@test -d $(ENV_DIR) || (echo "ERROR: Missing $(ENV_DIR)"; exit 1)
	@test -f $(VAULT_FILE) || (echo "ERROR: Missing $(VAULT_FILE)"; exit 1)

ping: env-check
	ansible $(GROUP) -i $(INV) -m ping $(EXTRA_VARS) $(ANS_VAULT)

plan: env-check
	$(PLAY) playbook.yml -i $(INV) --check $(EXTRA_VARS) $(ANS_VAULT)

apply: env-check
	$(PLAY) playbook.yml -i $(INV) $(EXTRA_VARS) $(ANS_VAULT)

# Edit in place (safe: decrypts in your editor and re-encrypts on save)
vault-edit:
	ansible-vault edit $(VAULT_FILE) $(VAULT_PASS)

# Encrypt the file in-place
vault-encrypt:
	ansible-vault encrypt $(VAULT_FILE) $(VAULT_PASS)

# Permanently decrypt the file (not recommended to commit!)
vault-decrypt:
	ansible-vault decrypt $(VAULT_FILE) $(VAULT_PASS)

# View the vault contents without writing to disk
vault-view:
	ansible-vault view $(VAULT_FILE) $(VAULT_PASS)

# Decrypt to a temporary file in /tmp and show its path
vault-decrypt-tmp:
	@f=$$(mktemp /tmp/$(ENV)-vault.XXXXXX.yml); \
	ansible-vault view $(VAULT_FILE) $(VAULT_PASS) > $$f && chmod 600 $$f && \
	echo "Decrypted to $$f"

# Change the vault password (rekey)
vault-rekey:
	ansible-vault rekey $(VAULT_FILE) $(VAULT_PASS)
