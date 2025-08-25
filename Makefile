SHELL := /bin/bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c

# Minimal bootstrap: fetch only one subdirectory from a remote repo
REPO   ?= https://github.com/Gwozdziuuu/setup.git
REF    ?= main
SUBDIR ?= project/stack-init
DEST   ?= .

.PHONY: stack-init
stack-init:
	TMP=$$(mktemp -d 2>/dev/null || mktemp -d -t stack-init)
	echo ">> Fetching '$(SUBDIR)' from $(REPO) @ $(REF)"
	git clone --depth=1 --filter=blob:none --sparse -b "$(REF)" "$(REPO)" "$$TMP/src" >/dev/null
	git -C "$$TMP/src" sparse-checkout set --no-cone "$(SUBDIR)" >/dev/null
	test -d "$$TMP/src/$(SUBDIR)" || { echo "ERROR: subdir '$(SUBDIR)' not found"; exit 1; }
	mkdir -p "$(DEST)"
	if command -v rsync >/dev/null 2>&1; then
	  rsync -a "$$TMP/src/$(SUBDIR)/" "$(DEST)/"
	else
	  (cd "$$TMP/src/$(SUBDIR)" && tar cf - .) | (cd "$(DEST)" && tar xf -)
	fi
	rm -rf "$$TMP"
	echo ">> Done. Copied to: $(abspath $(DEST))"

.PHONY: quarkus-init
quarkus-init:
	TMP=$$(mktemp -d 2>/dev/null || mktemp -d -t quarkus)
	echo ">> Fetching 'project/quarkus' from $(REPO) @ $(REF)"
	git clone --depth=1 --filter=blob:none --sparse -b "$(REF)" "$(REPO)" "$$TMP/src" >/dev/null
	git -C "$$TMP/src" sparse-checkout set --no-cone "project/quarkus" >/dev/null
	test -d "$$TMP/src/project/quarkus" || { echo "ERROR: subdir 'project/quarkus' not found"; exit 1; }
	mkdir -p "../../quarkus"
	if command -v rsync >/dev/null 2>&1; then
	  rsync -a "$$TMP/src/project/quarkus/" "../quarkus/"
	else
	  (cd "$$TMP/src/project/quarkus" && tar cf - .) | (cd "../quarkus" && tar xf -)
	fi
	rm -rf "$$TMP"
	echo ">> Done. Copied to: $(abspath ../quarkus)"
