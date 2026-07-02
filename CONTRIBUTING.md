# Contributing to VaultFlow-Core

## Commits

This project uses **Conventional Commits** para determinar bumps de versión y generar el CHANGELOG automáticamente.

### Formato

```
<type>(<scope opcional>): <descripción en presente>

[cuerpo opcional]

[footer opcional (ej: Closes #123)]
```

### Types permitidos

| Type       | Uso                                              | Bump  |
|------------|--------------------------------------------------|-------|
| `feat`     | Nueva funcionalidad                              | minor |
| `fix`      | Corrección de bug                                | patch |
| `docs`     | Cambios en documentación                         | patch |
| `style`    | Formato, espacios, lint (no lógico)              | —     |
| `refactor` | Cambio que no agrega feature ni corrige bug      | —     |
| `perf`     | Mejora de rendimiento                            | patch |
| `test`     | Tests nuevos o corregidos                        | —     |
| `ci`       | Cambios en CI/CD                                 | —     |
| `chore`    | Mantenimiento, tooling, dependencias             | —     |
| `revert`   | Revertir un commit anterior                      | —     |

### Ejemplos

```
feat(auth): add OAuth2 Google login
fix(wallet): prevent negative balance on transfer
docs: update API endpoints in README
ci: add pre-release support to docker publish
```

> Los commits son validados automáticamente por **commitlint + husky** via hook pre-commit.

---

## Versionado Semántico (SemVer)

Usamos `MAJOR.MINOR.PATCH`:

- **MAJOR** — cambios incompatibles con versiones anteriores
- **MINOR** — nuevas funcionalidades backwards-compatible
- **PATCH** — bug fixes backwards-compatible

La versión actual se define en `VERSION` (single source of truth).

### Pre-releases

Para releases en staging/testing se usa el formato SemVer con sufijo:

```
v1.2.0-alpha.1    # Desarrollo temprano
v1.2.0-beta.2     # Feature-complete, en testing
v1.2.0-rc.3       # Release candidate
v1.2.0            # Stable release
```

Los tags pre-release (`v*-*`) no sobreescriben el tag Docker `:latest`.

### Bump de versión automático

```bash
# Desde la raíz del proyecto:
npm run release          # bump automático según commits
npm run release:dry      # simulación (dry-run)
npm run release:minor    # forzar minor bump
npm run release:major    # forzar major bump
```

O manualmente:

```bash
./scripts/bump-version.sh 1.2.0
git add VERSION backend/pom.xml frontend/package.json
git commit -m "chore(version): bump to 1.2.0"
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin v1.2.0
```

---

## Tags

Siempre usar **annotated tags** para releases:

```bash
# Correcto (annotated):
git tag -a v1.2.0 -m "Release v1.2.0 - descripción"

# Incorrecto (lightweight):
git tag v1.2.0 -m "desc"
```

Los annotated tags incluyen: autor, fecha, mensaje, y soportan GPG signing.

### GPG Signing (opcional pero recomendado)

```bash
# Configurar GPG key
git config --global user.signingkey <KEY>
git config --global commit.gpgsign true
git config --global tag.gpgsign true

# Firmar un tag de release
git tag -a v1.2.0 -m "Release v1.2.0" --sign
```

---

## Ramas

| Rama         | Desde     | Mergea a   | Propósito                    |
|--------------|-----------|------------|------------------------------|
| `feature/*`  | `develop` | `develop`  | Nueva funcionalidad          |
| `fix/*`      | `develop` | `develop`  | Corrección de errores        |
| `release/*`  | `develop` | `main`     | Preparación de release       |
| `develop`    | —         | `main`     | Integración diaria           |
| `main`       | —         | —          | Releases oficiales           |
| `deploy`     | `main`    | —          | Apunta al último tag liberado|

### Convención de nombres

- `feature/kebab-case-de-la-feature`
- `fix/descripcion-del-fix`
- `release/vX.Y.Z`

---

## Pull Requests

1. Crear branch desde `develop`
2. Implementar cambios con commits convencionales
3. Verificar tests: `./mvnw test` (backend) y `npm run build` (frontend)
4. Crear PR a `develop` con el template completado
5. Referenciar el issue correspondiente: `Closes #123`

## Issues

Cada cambio debe estar vinculado a un issue de GitHub. Los commits deben cerrarlos automáticamente:

```
Closes #1
Closes #2, Closes #3
```

---

## Release Process

### 1. Preparación

```bash
git checkout develop
# Verificar que develop está listo (features mergeadas, tests verdes)
```

### 2. Taggear

```bash
git checkout main
git merge develop
git tag -a v1.2.0 -m "Release v1.2.0 - Descripción de cambios"
git push origin v1.2.0
```

### 3. CI automático

Al pushear el tag, el pipeline:

1. Ejecuta tests (backend + frontend)
2. Build JAR y frontend
3. Crea GitHub Release con artifacts adjuntos
4. Publica imágenes Docker (si hay credenciales)
5. Avanza rama `deploy`

---

## Docker

Los tags Docker siguen el formato:

| Tag                          | Propósito      |
|------------------------------|----------------|
| `:latest`                    | Último release estable |
| `:X.Y.Z`                     | Versión específica     |
| `:X.Y.Z-alpha.N`             | Pre-release   |
| `:X.Y.Z-beta.N`              | Pre-release   |
| `:X.Y.Z-rc.N`                | Release candidate |

Los pre-releases no sobreescriben el tag `:latest`.
