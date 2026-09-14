# Contributing to Lite++

Thank you for your interest in contributing to Lite++.

## Code Style & Conventions

- Follow standard Android and Java conventions.
- Use PascalCase for class names and camelCase for methods and variables.
- Write clean, self-documenting code without redundant comments.
- Keep dependencies and changes minimal and focused.

## Icon Guidelines

Lite++ uses [Phosphor Icons](https://phosphoricons.com) for all application drawables. When adding or updating icons:

- Source all icons directly from the official [Phosphor Icons](https://github.com/phosphor-icons/core) repository.
- Style convention: Use `regular` (outline) for actions, navigation, and controls; use `fill` for solid category anchors (e.g. theme, database, cache).
- Viewport and sizing: Vector drawables must be `24dp` by `24dp` with `viewportWidth="256"` and `viewportHeight="256"`.
- Naming convention: Follow the strict `ic_<name>.xml` format in `app/src/main/res/drawable/`.
- Tinting: Set `android:tint="?attr/colorAccent"` (or theme-appropriate tint attribute) on the vector root and use `android:fillColor="#FFFFFFFF"` on path elements.

## Development Workflow

1. Fork the repository and create a feature branch:
   ```sh
   git checkout -b feature/your-feature-name
   ```
2. Make your changes and test on an Android device or emulator.
3. Validate that the project builds cleanly without errors:
   ```sh
   ./gradlew compileDebugJavaWithJavac
   ```
4. Commit your changes with clear, descriptive commit messages following the Conventional Commits format (`feat:`, `fix:`, `refactor:`, `style:`).
5. Open a pull request against the `main` branch.
