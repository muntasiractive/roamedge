<div align="center">
  <img src="src/main/resources/icons/roam-icon.png" alt="Roam Logo" width="128" height="128">
  <h1>🚀 Roam</h1>
  <p><strong>A Modern Desktop Productivity Suite</strong></p>

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-007396?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

<br/>

[Features](#-features) • [Screenshots](#-screenshots) • [Installation](#-installation) • [Usage](#-usage) • [Structure](#-structure)

</div>

---

## 📋 Overview

**Roam** is a desktop application designed to help you take control of your productivity. It brings together operations, tasks, calendar, wikis, and journaling into one clean, offline-first workspace.

### Why use Roam?

- **🎯 Integrated**: Manage projects, tasks, and notes in one place.
- **🔒 Private**: Your data stays on your machine, encrypted and PIN-protected.
- **⚡ Fast**: Built for desktop performance with instant search.
- **🎨 Modern**: A refined interface that supports dark mode.

---

## 🎯 Features

### 📊 Operations

Projects are top-level "Operations". Track their status, priority, and purpose.

### ✅ Tasks

Manage tasks with multiple views:

- **Kanban Board** for workflow
- **Card View** for quick scanning
- **Eisenhower Matrix** for prioritization

### 📅 Calendar

Schedule events and sync them with your tasks and operations.

### 📚 Wiki

Write rich notes with Markdown support. Link articles together using `[[Wiki Links]]`.

### 📓 Journal

A dedicated space for daily reflection and logging.

### 🔍 Global Search

Press `Ctrl+K` to search everything instantly.

---

## 📸 Screenshots

<div align="center">

|                              Dashboard                              |                              Operations                              |
| :-----------------------------------------------------------------: | :------------------------------------------------------------------: |
| ![Dashboard](<src/main/resources/screenshots/screenshot%20(1).png>) | ![Operations](<src/main/resources/screenshots/screenshot%20(2).png>) |

|                          Kanban Board                           |                              Calendar                              |
| :-------------------------------------------------------------: | :----------------------------------------------------------------: |
| ![Tasks](<src/main/resources/screenshots/screenshot%20(3).png>) | ![Calendar](<src/main/resources/screenshots/screenshot%20(4).png>) |

</div>

---

## 📥 Installation

### Prerequisites

- **Java JDK 21+**
- **Gradle 8.x** (wrapper included)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/muntasiractive/roamedge.git
cd roamedge

# Build and run
./gradlew run
```

---

## 📖 Usage

1. **First Launch**: Set up your secure PIN.
2. **Navigation**: Use the floating bar or shortcuts.
3. **Shortcuts**:
   - `Ctrl + N`: New Item
   - `Ctrl + K`: Universal Search
   - `Ctrl + D`: Toggle Theme

---

## 🏗️ Structure

The project follows a standard MVC architecture:

```
src/main/java/com/roam/
├── controller/     # Logic handling
├── model/          # Data entities (Hibernate/JPA)
├── view/           # JavaFX UI views
├── service/        # Business logic
└── repository/     # Database access
```

---

## 📄 License

This project is licensed under the **MIT License**.

---

<div align="center">
  <p>Star this repo if you find it useful! ⭐</p>
</div>
