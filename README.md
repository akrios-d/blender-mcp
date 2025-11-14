# Blender MCP (Java Port)

This repository is a **Java-based port** of the original Blender Open MCP project by [dhakalnirajan](https://github.com/dhakalnirajan/blender-open-mcp/tree/main). The goal of this project is to bring the core features of the MCP system into a Java environment while maintaining compatibility with the existing Blender add-on structure using Spring AI.

## 📦 Project Overview

This project ports the main logic and functionality from the original MCP implementation into Java. It retains the original `addon.py` file from the source repository to ensure compatibility with Blender's Python-based add-on architecture.

### 🔧 What Has Been Ported

* Core MCP logic rewritten in Java
* Refactoring for improved structure and readability
* Integration layer allowing Java logic to be used alongside the existing Python add-on

### 🧩 What Remains from the Original Repo

* `dhakalnirajan.py` is still used as-is

## 📁 Repository Structure

```
b l e n d e r - m c p /
├── java/                  # Ported Java source code
├── addon/                 # Python add-on structure
│   └── dhakalnirajan.py           # Original add-on file retained
└── resources/             # Any additional assets
```

## 🚀 Getting Started

### Requirements

* **Java 17+** (or the version you target)
* **Blender 3.x+**
* Python environment included with Blender

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/akrios-d/blender-mcp
   ```
2. Build or run the Java module as needed.
3. Install the Blender add-on by loading the folder containing `dhakalnirajan.py`.

## 🧪 Usage

**Install the Blender Add-on:**

- Open Blender.
- Go to `Edit -> Preferences -> Add-ons`.
- Click `Install...`.
- Select the `dhakalnirajan.py` file from the `addon` directory.
- Enable the "Blender MCP" add-on.

**Start the Blender Add-on Server:**

- Open Blender and the 3D Viewport.
- Press `N` to open the sidebar.
- Find the "Blender MCP" panel.
- Click "Start MCP Server".

## 🤝 Acknowledgements

Special thanks to **dhakalnirajan** for the original Blender Open MCP implementation.

Original repository: [https://github.com/dhakalnirajan/blender-open-mcp/tree/main](https://github.com/dhakalnirajan/blender-open-mcp/tree/main)

### Send a Prompt to the Agent

You can instruct Blender using natural language. Example:

```bash
curl -X POST http://localhost:8080/api/agent/run \
  -H "Content-Type: application/json" \
  -d '{"prompt": "create a red cube and render it"}'
```

Expected example output:

```
Created object: Cube
Applied material: Red
Rendered image (Base64): <...>
```

### What Works Today

| Tool Name                  | Description                            | Parameters                                            |
| -------------------------- | -------------------------------------- | ----------------------------------------------------- |
| `get_scene_info`           | Retrieves scene details.               | None                                                  |
| `get_object_info`          | Retrieves information about an object. | `object_name` (str)                                   |
| `create_object`            | Creates a 3D object.                   | `type`, `name`, `location`, `rotation`, `scale`       |
| `modify_object`            | Modifies an object’s properties.       | `name`, `location`, `rotation`, `scale`, `visible`    |
| `delete_object`            | Deletes an object.                     | `name` (str)                                          |
| `set_material`             | Assigns a material to an object.       | `object_name`, `material_name`, `color`               |
| `render_image`             | Renders an image.                      | `file_path` (str)                                     |
| `execute_blender_code`     | Executes Python code in Blender.       | `code` (str)                                          |
| `get_polyhaven_categories` | Lists PolyHaven asset categories.      | `asset_type` (str)                                    |
| `search_polyhaven_assets`  | Searches PolyHaven assets.             | `asset_type`, `categories`                            |
| `download_polyhaven_asset` | Downloads a PolyHaven asset.           | `asset_id`, `asset_type`, `resolution`, `file_format` |
| `set_texture`              | Applies a downloaded texture.          | `object_name`, `texture_id`                           |
| `set_ollama_model`         | Sets the Ollama model.                 | `model_name` (str)                                    |
| `set_ollama_url`           | Sets the Ollama server URL.            | `url` (str)                                           |
| `get_ollama_models`        | Lists available Ollama models.         | None                                                  |

---

## 🔮 Future Work

A roadmap for where the project is heading.

### 1. Convert to a Full Spring MCP Server

Move from the current REST-based workflow to a true MCP server:

* Proper MCP tools
* Standard message envelopes
* Cleaner function-like interactions

### 2. Integrate With `ahujasid/blender-mcp`

Repository:

```
https://github.com/ahujasid/blender-mcp
```