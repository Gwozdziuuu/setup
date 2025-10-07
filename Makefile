SHELL := /bin/bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c

# Minimal bootstrap: fetch only one subdirectory from a remote repo
REPO   ?= https://github.com/Gwozdziuuu/setup.git
REF    ?= main
SUBDIR ?= project/stack-init
DEST   ?= .

# ----- Quarkus project defaults (samodzielny projekt) -----
Q_GROUP    ?= com.example
Q_ARTIFACT ?= quarkus-app
Q_PACKAGE  ?= $(Q_GROUP).app
Q_EXT      ?= resteasy-reactive,smallrye-openapi,smallrye-health
Q_BUILD    ?= maven          # maven | gradle
Q_JAVA     ?= 21
Q_DIR      ?= ../$(Q_ARTIFACT)

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

# ----- NOWA NAZWA: import wycinka repo (sparse-checkout) -----
.PHONY: quarkus-import-subdir quarkus-init
quarkus-import-subdir:
	@if [ -d "../quarkus" ]; then echo ">> Directory ../quarkus already exists. Remove it first."; exit 1; fi
	echo ">> Importing from $(REPO) @ $(REF) (sparse-checkout: 'project/quarkus')"
	git clone --filter=blob:none --sparse -b "$(REF)" "$(REPO)" "../quarkus" >/dev/null
	git -C "../quarkus" sparse-checkout set --no-cone "project/quarkus" >/dev/null
	test -d "../quarkus/project/quarkus" || { echo "ERROR: subdir 'project/quarkus' not found"; exit 1; }
	echo ">> Done. Repository ready at: $(abspath ../quarkus) (sparse-checkout: project/quarkus only)"

# Alias wsteczny + komunikat o deprecacji
quarkus-init: quarkus-import-subdir
	@echo ">> [DEPRECATED] Użyj celu 'quarkus-import-subdir' (ten pozostaje jako alias)."

# ----- NOWY CEL: samodzielny projekt Quarkusa -----
.PHONY: quarkus-init-project
quarkus-init-project:
	@if [ -e "$(Q_DIR)" ]; then echo ">> Directory $(Q_DIR) already exists. Remove it or set Q_DIR=..."; exit 1; fi
	echo ">> Creating Quarkus project $(Q_GROUP):$(Q_ARTIFACT)"
	echo ">> Output: $(abspath $(Q_DIR)) | Build: $(Q_BUILD) | Java: $(Q_JAVA)"
	echo ">> Extensions: $(Q_EXT) | Package: $(Q_PACKAGE)"
	if command -v quarkus >/dev/null 2>&1; then \
	  echo ">> Using Quarkus CLI"; \
	  quarkus create app "$(Q_GROUP):$(Q_ARTIFACT)" \
	    -x "$(Q_EXT)" \
	    --java="$(Q_JAVA)" \
	    --package-name="$(Q_PACKAGE)" \
	    --output-directory="$(Q_DIR)" \
	    --"$(Q_BUILD)"; \
	else \
	  echo ">> Quarkus CLI not found; falling back to Maven plugin"; \
	  mvn -B io.quarkus.platform:quarkus-maven-plugin:create \
	    -DprojectGroupId="$(Q_GROUP)" \
	    -DprojectArtifactId="$(Q_ARTIFACT)" \
	    -DpackageName="$(Q_PACKAGE)" \
	    -DclassName="$(Q_PACKAGE).GreetingResource" \
	    -Dpath="/hello" \
	    -Dextensions="$(Q_EXT)" \
	    -DjavaVersion="$(Q_JAVA)" \
	    -DbuildTool="$(Q_BUILD)"; \
	  mv "$(Q_ARTIFACT)" "$(Q_DIR)"; \
	fi
	echo ">> Done. Project at: $(abspath $(Q_DIR))"

# ----- Spring project defaults (standalone project) -----
S_GROUP    ?= com.example
S_ARTIFACT ?= spring-app
S_PACKAGE  ?= $(S_GROUP).app
S_JAVA     ?= 21
S_DIR      ?= ../$(S_ARTIFACT)

.PHONY: spring-init-project
spring-init-project:
	@if [ -e "$(S_DIR)" ]; then echo ">> Directory $(S_DIR) already exists. Remove it or set S_DIR=..."; exit 1; fi
	echo ">> Creating Spring project from $(REPO) @ $(REF)"
	echo ">> Output: $(abspath $(S_DIR))"
	TMP=$$(mktemp -d 2>/dev/null || mktemp -d -t spring-init)
	echo ">> Sparse-checkout: project/spring"
	git clone --filter=blob:none --sparse -b "$(REF)" "$(REPO)" "$$TMP/src" >/dev/null
	git -C "$$TMP/src" sparse-checkout set --no-cone "project/spring" >/dev/null
	test -d "$$TMP/src/project/spring" || { echo "ERROR: subdir 'project/spring' not found"; rm -rf "$$TMP"; exit 1; }
	mkdir -p "$(S_DIR)"
	if command -v rsync >/dev/null 2>&1; then \
	  rsync -a "$$TMP/src/project/spring/" "$(S_DIR)/"; \
	else \
	  (cd "$$TMP/src/project/spring" && tar cf - .) | (cd "$(S_DIR)" && tar xf -); \
	fi
	rm -rf "$$TMP"
	echo ">> Removing git dependencies"
	rm -rf "$(S_DIR)/.git"
	find "$(S_DIR)" -name ".gitkeep" -type f -delete 2>/dev/null || true
	echo ">> Done. Standalone Spring project at: $(abspath $(S_DIR))"
