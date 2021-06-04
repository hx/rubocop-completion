# <img src="src/main/resources/META-INF/pluginIcon.svg" width="48"> RuboCop Completion

![Build](https://github.com/hx/rubocop-completion/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16929-rubocop-completion.svg)](https://plugins.jetbrains.com/plugin/16929-rubocop-completion)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16929-rubocop-completion.svg)](https://plugins.jetbrains.com/plugin/16929-rubocop-completion)

<!-- Plugin description -->
Autocompletion and validation of RuboCop configuration files, and inline enable/disable/todo directives.

Your `Gemfile.lock` is used to determine RuboCop and extension versions, so you should always have the right list of cops.

The [`rubocop-schema-gen`](https://github.com/hx/rubocop-schema) gem is used to generate schemas from RuboCop's documentation.
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "rubocop-completion"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/hx/rubocop-completion/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
