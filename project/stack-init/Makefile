GALAXY=ansible-galaxy
PLAY=ansible-playbook
INV=inventory/hosts.yml

ENV ?= dev
STACK_FILE=inventory/group_vars/stack.yml
VAULT_FILE=inventory/group_vars/$(ENV)/vault.yml

VAULT_PASS ?=
ANS_VAULT=$(if $(VAULT_PASS),$(VAULT_PASS),--ask-vault-pass)
EXTRA_VARS=-e @$(STACK_FILE) -e @$(VAULT_FILE)

GROUP ?= db

help:
	@echo "Usage: make <target> [ENV=dev|stage|prod] [VAULT_PASS=--vault-password-file=.vault_pass.txt] [EXTRA='-e foo=bar']"
	@echo "Targets: init, ping, plan, apply, vault-edit, vault-encrypt, vault-decrypt, vault-view, vault-decrypt-tmp, vault-rekey"

init:
	$(GALAXY) install -r requirements.yml --force

env-check:
	@echo ">> ENV=$(ENV)"
	@test -f $(STACK_FILE) || (echo "ERROR: Missing $(STACK_FILE)"; exit 1)
	@test -f $(VAULT_FILE) || (echo "ERROR: Missing $(VAULT_FILE)"; exit 1)

ping: env-check
	ansible $(GROUP) -i $(INV) -m ping $(EXTRA_VARS) $(ANS_VAULT) $(EXTRA)

plan: env-check
	$(PLAY) playbook.yml -i $(INV) --check $(EXTRA_VARS) $(ANS_VAULT) $(EXTRA)

apply: env-check
	$(PLAY) playbook.yml -i $(INV) $(EXTRA_VARS) $(ANS_VAULT) $(EXTRA)

vault-edit:
	ansible-vault edit $(VAULT_FILE) $(VAULT_PASS)
vault-encrypt:
	ansible-vault encrypt $(VAULT_FILE) $(VAULT_PASS)
vault-decrypt:
	ansible-vault decrypt $(VAULT_FILE) $(VAULT_PASS)
vault-view:
	ansible-vault view $(VAULT_FILE) $(VAULT_PASS)
vault-decrypt-tmp:
	@f=$$(mktemp /tmp/$(ENV)-vault.XXXXXX.yml); \
	ansible-vault view $(VAULT_FILE) $(VAULT_PASS) > $$f && chmod 600 $$f && \
	echo "Decrypted to $$f"
vault-rekey:
	ansible-vault rekey $(VAULT_FILE) $(VAULT_PASS)
