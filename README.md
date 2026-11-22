# CustomWorlds (Paper 26.3, Java 25)

Сборка: `gradle build` -> `build/libs/CustomWorlds.jar` -> в папку `plugins/`.

Структуры: обычный мир (склеп, башня, павильон), ад (бункер, кузня, алтарь), энд (святилище, зловещее хранилище).
Расстояния между структурами и шансы — `plugins/CustomWorlds/config.yml`, лут — `Loot.java`.

## Заменить стандартные миры (bukkit.yml, удалить старые папки миров)
```
worlds:
  world:
    generator: CustomWorlds:normal
  world_nether:
    generator: CustomWorlds:nether
  world_the_end:
    generator: CustomWorlds:end
```
Доп. миры: `/cw create <имя> <normal|nether|end>`, `/cw tp <имя>`
