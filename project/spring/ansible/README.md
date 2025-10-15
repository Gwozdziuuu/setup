# Ansible CI/CD Playbooks

Repozytorium kroków CI/CD napisanych w Ansible, które mogą być wykorzystane niezależnie od platformy CI/CD (GitHub Actions, GitLab CI, Jenkins, itp.).

## Struktura

```
ansible/
├── ansible.cfg              # Konfiguracja Ansible
├── requirements.yml         # Wymagane kolekcje Ansible
├── inventory/
│   └── localhost.yml       # Inventory dla lokalnego wykonania
├── group_vars/
│   └── all.yml            # Globalne zmienne
├── playbooks/
│   ├── build.yml          # Budowanie projektu Maven
│   ├── test.yml           # Uruchamianie testów
│   ├── quality-check.yml  # Sprawdzanie jakości kodu
│   ├── package.yml        # Pakowanie aplikacji
│   ├── docker-build.yml   # Budowanie obrazu Docker
│   └── ci-pipeline.yml    # Kompletny pipeline CI
└── roles/                 # Miejsce na role Ansible (opcjonalne)
```

## Wymagania

- Python 3.11+
- Ansible 2.15+
- Java 21 (dla projektów Spring)
- Maven 3.x
- Docker (opcjonalnie, dla docker-build.yml)

## Instalacja

1. Zainstaluj Ansible:
```bash
pip install ansible==2.15.*
```

2. Zainstaluj wymagane kolekcje:
```bash
cd ansible
ansible-galaxy collection install -r requirements.yml
```

## Filozofia i optymalizacja

Ten zestaw playbooków został zaprojektowany aby **unikać redundantnej kompilacji** aplikacji. Maven ma swój lifecycle, który automatycznie wykonuje wcześniejsze fazy:
- `mvn test` → automatycznie wykonuje `compile`
- `mvn package` → automatycznie wykonuje `compile` i `test` (lub pomija testy z `-DskipTests`)

### Tryby działania CI pipeline

#### 1. Build bez Dockera (tradycyjny)
```bash
ansible-playbook playbooks/ci-pipeline.yml -e "build_docker=false"
```
**Kolejność:**
1. Quality checks (statyczna analiza na źródłach)
2. Tests (mvn test - kompiluje i testuje)
3. Package (mvn package - pakuje do JAR)

#### 2. Build z Docker multi-stage (zoptymalizowany - REKOMENDOWANY dla CI)
```bash
ansible-playbook playbooks/ci-pipeline.yml -e "build_docker=true" -e "docker_multistage=true"
```
**Kolejność:**
1. Quality checks (statyczna analiza na źródłach)
2. Tests (mvn test - uruchamia testy, generuje raporty/coverage dla CI)
3. Docker build (multi-stage Dockerfile buduje aplikację z -DskipTests)

**Zalety:**
- Artefakty testów (raporty, coverage) dostępne w CI
- Docker build jest szybszy (testy już przeszły)
- Nie testujemy dwa razy (raz w CI, raz w Dockerze)
- Reprodukowalny build aplikacji w kontenerze

#### 3. Build z Docker simple (kopiuje JAR)
```bash
ansible-playbook playbooks/ci-pipeline.yml -e "build_docker=true" -e "docker_multistage=false"
```
**Kolejność:**
1. Quality checks
2. Tests
3. Package (buduje JAR lokalnie)
4. Docker build (kopiuje gotowy JAR do obrazu)

**Zalety:** Szybszy re-build jeśli JAR jest już zbudowany, mniejszy obraz końcowy.

### Dockerfiles

Projekt zawiera dwa Dockerfile:

- **`Dockerfile`** - Prosty, kopiuje gotowy JAR z `target/` (szybszy, wymaga pre-build)
- **`Dockerfile.multistage`** - Multi-stage, buduje aplikację od zera w kontenerze (reprodukowalny)
  - Wspiera build arg `SKIP_TESTS` (default: true)
  - `SKIP_TESTS=true` - buduje z `-DskipTests` (szybszy, dla CI z testami przed Dockerem)
  - `SKIP_TESTS=false` - buduje z testami w kontenerze (pełna izolacja)

**Rekomendacja dla CI:** Użyj `Dockerfile.multistage` z `SKIP_TESTS=true` i uruchom testy przed Docker buildem.

## Dostępne playbooki

### 1. build.yml - Budowanie projektu (opcjonalny)
Kompiluje projekt Maven.

```bash
ansible-playbook playbooks/build.yml
```

Parametry:
- `skip_tests` (default: true) - Pomija testy podczas budowania
- `maven_goals` (default: "clean compile") - Cele Maven do wykonania

Przykład z parametrami:
```bash
ansible-playbook playbooks/build.yml -e "skip_tests=false" -e "maven_goals='clean install'"
```

### 2. test.yml - Uruchamianie testów
Uruchamia testy jednostkowe i integracyjne.

```bash
ansible-playbook playbooks/test.yml
```

Parametry:
- `test_goals` (default: "test") - Cele testowe Maven

### 3. quality-check.yml - Sprawdzanie jakości kodu
Uruchamia narzędzia do sprawdzania jakości kodu.

```bash
ansible-playbook playbooks/quality-check.yml
```

Parametry:
- `run_checkstyle` (default: true) - Uruchom Checkstyle
- `run_spotbugs` (default: false) - Uruchom SpotBugs
- `run_pmd` (default: false) - Uruchom PMD

Przykład:
```bash
ansible-playbook playbooks/quality-check.yml -e "run_spotbugs=true" -e "run_pmd=true"
```

### 4. package.yml - Pakowanie aplikacji
Pakuje aplikację do pliku JAR.

```bash
ansible-playbook playbooks/package.yml
```

Parametry:
- `skip_tests` (default: true) - Pomija testy podczas pakowania
- `package_goals` (default: "package") - Cele pakowania Maven

### 5. docker-build.yml - Budowanie obrazu Docker
Buduje obraz Docker aplikacji. Obsługuje dwa tryby:
- **Multi-stage** (`use_multistage=true`) - buduje aplikację od zera w kontenerze (używa `Dockerfile.multistage`)
- **Simple** (`use_multistage=false`) - kopiuje gotowy JAR do obrazu (używa `Dockerfile`)

```bash
# Multi-stage build (buduje wszystko w Dockerze, z testami)
ansible-playbook playbooks/docker-build.yml -e "use_multistage=true" -e "skip_tests_in_docker=false"

# Multi-stage build (buduje bez testów - gdy testy już w CI)
ansible-playbook playbooks/docker-build.yml -e "use_multistage=true" -e "skip_tests_in_docker=true"

# Simple build (kopiuje gotowy JAR)
ansible-playbook playbooks/docker-build.yml -e "use_multistage=false"
```

Parametry:
- `use_multistage` (default: true) - Użyj multi-stage Dockerfile
- `skip_tests_in_docker` (default: true) - Pomija testy w Docker build (dla multi-stage)
- `image_name` (default: "spring-app") - Nazwa obrazu
- `image_tag` (default: "latest") - Tag obrazu
- `push_image` (default: false) - Czy wypchać obraz do registry
- `registry` (default: "") - Adres registry Docker

Przykład z push do registry:
```bash
ansible-playbook playbooks/docker-build.yml \
  -e "use_multistage=true" \
  -e "image_name=my-app" \
  -e "image_tag=1.0.0" \
  -e "push_image=true" \
  -e "registry=docker.io/myuser"
```

### 6. ci-pipeline.yml - Kompletny pipeline
Uruchamia wszystkie kroki w odpowiedniej kolejności. Inteligentnie wybiera które kroki wykonać w zależności od parametrów, **unikając redundantnej kompilacji**.

```bash
# Tradycyjny build (bez Dockera)
ansible-playbook playbooks/ci-pipeline.yml

# Build z Docker multi-stage (zoptymalizowany)
ansible-playbook playbooks/ci-pipeline.yml -e "build_docker=true" -e "docker_multistage=true"

# Build z Docker simple
ansible-playbook playbooks/ci-pipeline.yml -e "build_docker=true" -e "docker_multistage=false"
```

Parametry:
- `skip_tests` (default: false) - Pomija testy
- `run_quality_checks` (default: true) - Uruchamia sprawdzanie jakości kodu
- `build_docker` (default: false) - Buduje obraz Docker
- `docker_multistage` (default: true) - Użyj multi-stage Dockerfile (gdy `build_docker=true`)

**Logika wykonania:**
- Jeśli `build_docker=true` i `docker_multistage=true`: quality checks + **tests (artefakty w CI)** + docker build (-DskipTests)
- Jeśli `build_docker=true` i `docker_multistage=false`: quality checks + tests + package + docker build (copy JAR)
- Jeśli `build_docker=false`: quality checks + tests + package

**Kluczowa optymalizacja:** W trybie multi-stage testy uruchamiają się PRZED Docker buildem, dzięki czemu:
- CI ma dostęp do raportów testów i coverage
- Docker build jest szybszy (pomija testy)
- Nie testujemy dwukrotnie

## Użycie w różnych systemach CI/CD

### GitHub Actions

Zobacz pliki `.github/workflows/ci.yml` i `.github/workflows/manual-steps.yml` dla przykładów.

### GitLab CI

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - package

build:
  stage: build
  image: python:3.11
  before_script:
    - pip install ansible==2.15.*
    - cd ansible && ansible-galaxy collection install -r requirements.yml
  script:
    - cd ansible && ansible-playbook playbooks/build.yml

test:
  stage: test
  image: python:3.11
  before_script:
    - pip install ansible==2.15.*
    - cd ansible && ansible-galaxy collection install -r requirements.yml
  script:
    - cd ansible && ansible-playbook playbooks/test.yml
```

### Jenkins

```groovy
pipeline {
    agent any

    stages {
        stage('Setup') {
            steps {
                sh 'pip install ansible==2.15.*'
                sh 'cd ansible && ansible-galaxy collection install -r requirements.yml'
            }
        }

        stage('Build') {
            steps {
                sh 'cd ansible && ansible-playbook playbooks/build.yml'
            }
        }

        stage('Test') {
            steps {
                sh 'cd ansible && ansible-playbook playbooks/test.yml'
            }
        }

        stage('Package') {
            steps {
                sh 'cd ansible && ansible-playbook playbooks/package.yml'
            }
        }
    }
}
```

### Lokalne uruchomienie

Możesz uruchamiać playbooki lokalnie podczas developmentu:

```bash
# Pełny pipeline
cd ansible
ansible-playbook playbooks/ci-pipeline.yml

# Tylko budowanie
ansible-playbook playbooks/build.yml

# Tylko testy
ansible-playbook playbooks/test.yml
```

## Dostosowywanie

### Zmienne globalne

Edytuj plik `group_vars/all.yml` aby zmienić domyślne wartości dla wszystkich playbooków.

### Dodawanie nowych kroków

1. Utwórz nowy playbook w `playbooks/`
2. Użyj wzorca z istniejących playbooków
3. Dodaj dokumentację do tego README

### Tworzenie ról

Dla bardziej skomplikowanej logiki, możesz utworzyć role Ansible w katalogu `roles/`:

```bash
ansible-galaxy init roles/my-custom-role
```

## Zalety tego podejścia

1. **Niezależność od platformy CI/CD** - Te same playbooki działają w GitHub Actions, GitLab CI, Jenkins, CircleCI, itp.
2. **Testowanie lokalne** - Możesz uruchomić całą logikę CI lokalnie
3. **Reużywalność** - Playbooki mogą być współdzielone między projektami
4. **Wersjonowanie** - Logika CI jest wersjonowana razem z kodem
5. **Modularność** - Każdy krok jest osobnym playbookiem, który można uruchamiać niezależnie

## Troubleshooting

### Problem z uprawnieniami Maven wrapper
```bash
ansible-playbook playbooks/build.yml
```
Playbook automatycznie nadaje uprawnienia wykonywania dla `mvnw`.

### Brak kolekcji Docker
```bash
cd ansible
ansible-galaxy collection install community.docker
```

### Testy nie przechodzą
```bash
ansible-playbook playbooks/test.yml -vvv
```
Użyj flagi `-vvv` dla szczegółowych logów.

## Dalsze kroki

- Dodaj własne playbooki dla deployment
- Utwórz role dla bardziej złożonej logiki
- Dodaj testy Ansible (molecule)
- Rozszerz o notification (Slack, email)