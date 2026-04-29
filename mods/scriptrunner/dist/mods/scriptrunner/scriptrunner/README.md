# Scriptrunner

Server-side JavaScript hot-reload runner for the WurmModLoader framework. Drop
`.js` files in `scripts/<trigger>/` and they execute when the corresponding
event fires — no build, no restart, no jar.

Use it for: quick balance tweaks, GM/admin commands, prototyping framework
hooks before committing to a Java mod, one-off live-server fixes.

## Layout

```
mods/scriptrunner/
├── scriptrunner/
│   ├── imports/                     ← shared helpers (require()-able)
│   │   ├── jvm-npm.js               ← CommonJS-style require() for Nashorn
│   │   └── actions.js               ← convenience wrappers
│   └── scripts/
│       ├── onPlayerLogin/           ← fires on PlayerLoginEvent
│       │   ├── greet.js.example
│       │   └── make-all-admins.js.example
│       ├── onPlayerMessage/         ← fires on chat/command messages
│       │   ├── gm.js.example        ← GM dispatcher (see below)
│       │   ├── add-action.js.example
│       │   ├── log.js.example
│       │   ├── test-question.js.example
│       │   └── test-ui.js.example
│       └── onModAction/             ← fires on every ModActionEvent
│           └── testui-action.js.example
└── mod.properties
```

Files are inert while suffixed `.example`. **Strip the suffix to enable.**

## Triggers

Each subdirectory under `scripts/` corresponds to a framework event. The script
must export a function with the matching name:

| Folder              | Function signature                                | Fires on                              |
|---------------------|---------------------------------------------------|---------------------------------------|
| `onPlayerLogin/`    | `onPlayerLogin(player)`                           | PlayerLoginEvent                      |
| `onPlayerLogout/`   | `onPlayerLogout(player)`                          | PlayerLogoutEvent                     |
| `onPlayerMessage/`  | `onPlayerMessage(communicator, message, title)`   | Chat / `/command` / local / GL etc.   |
| `onServerStarted/`  | `onServerStarted()`                               | ServerStartedEvent                    |
| `onServerPoll/`     | `onServerPoll()`                                  | Each server tick (~5s)                |
| `onModAction/`      | `onModAction(eventType, event)`                   | Every ModActionEvent (filter by type) |

Returning the string `"DISCARD"` from `onPlayerMessage` swallows the message so
it never reaches normal chat — useful for `/command` style scripts.

## Hot reload

Set in `mod.properties` (already configured for shipped triggers):

```
onPlayerMessage.refresh=true
onModAction.refresh=true
…
```

Scripts are re-read on every file mtime change — save the file and the next
event uses the new code. **No server restart needed.**

## Calling Java

Nashorn exposes the JVM directly:

```js
var ItemFactory    = Java.type("com.wurmonline.server.items.ItemFactory");
var EventBus       = Java.type("com.garward.wurmmodloader.core.event.EventBus");
var ModActionEvent = Java.type("com.garward.wurmmodloader.api.events.ModActionEvent");

var ev = new ModActionEvent("ui:open_window");
ev.set("player", player);
EventBus.getInstance().post(ev);
```

The mod runs with `sharedClassLoader=true`, so any framework, mod, or vanilla
Wurm class is reachable via `Java.type(...)`.

## The GM script (`gm.js.example`)

`scripts/onPlayerMessage/gm.js.example` is a ready-made dispatcher for live
testing on a running server. Strip `.example`, log in as a GM (power ≥ 2),
and run `/gm help` in chat.

Built-in subcommands:

| Command                          | What it does                                        |
|----------------------------------|-----------------------------------------------------|
| `/gm help`                       | List subcommands                                    |
| `/gm where`                      | Print your position, tile, surface/cave, kingdom    |
| `/gm tp <x> <y> [surface]`       | Teleport (meters; surface=true/false, default true) |
| `/gm power <0-5>`                | Set your GM power level                             |
| `/gm money <iron>`               | Add iron coins (100=1c, 10000=1s, 1000000=1g)       |
| `/gm item <tpl> [count] [ql] [r]`| Spawn item(s) into your inventory                   |
| `/gm heal`                       | Heal all wounds, refill hunger/thirst               |
| `/gm fire <event> [k=v ...]`     | Post a ModActionEvent (player auto-set as `player`) |
| `/gm ui <windowId>`              | Close a declarativeui window                        |

**Adding a subcommand:** append an entry to the `commands` map. Each handler
receives `(player, args[])` and returns a status string. Throw a string for
usage errors — the dispatcher catches it and prints with the registered
`usage` line. Hot-reload picks up edits on save.

`fire` is the secret weapon: it lets you smoke-test any `ModActionEvent`
channel (declarativeui, ui:close_window, custom mod events) without writing a
mod. For typed framework events that take `com.wurmonline.*` params, write a
dedicated subcommand that constructs the event directly.

## Permissions

The shipped GM script gates on `player.getPower() >= 2`. Edit `MIN_POWER` at
the top of the file to require HERO (1) or DEV (5) only. **Do not** ship a
script that exposes destructive commands without a power gate — `onPlayerMessage`
fires for every chat line from every player.

## Troubleshooting

* **Script doesn't run** — confirm the file has no `.example` suffix and lives
  under the correct trigger folder.
* **`ReferenceError: SomeClass is not defined`** — declare it via
  `Java.type("fully.qualified.Name")` at the top of the script.
* **Edits don't reload** — check the `<trigger>.refresh=true` line in
  `mod.properties` is present and uncommented.
* **Wrong `scriptsFolder`** — `mod.config` may override the default
  `scriptrunner/scripts` path. Default is correct for the layout above.

## Imports / require()

`imports/jvm-npm.js` is loaded automatically and exposes a CommonJS `require()`:

```js
var actions = require("actions");           // → imports/actions.js
```

Drop your own helpers in `imports/` and require them from any script.
