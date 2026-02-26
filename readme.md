# What does this mod do?
It adds a new structure "suspicious lake" that interconnects the whole end dimension.

## Structure
During world gen a structure is added named suspicious lake. It is made of End Stone, so-called Suspicious Liquid and air. Multiple structure files exist for this lake. They always spawn (or should at least) on their designated locations. Because of technical reasons, the lakes, although being lakes, are handled as structures, meaning they can be located with the /locate command.

## Linkage between the Suspicious Lakes
When using the lake to teleport (see [Suspicious Liquid](#suspicious-liquid)) you will teleport to the next lake inside the same group. When using the lake you land in over and over, you will land back where you started. The linkage depends on the seed meaning two worlds with the same seed (and the same world border) will have the same groups. Using a lake you will always land in the same lake (assuming the world border hasn't changed). Currently, changing the center of the world border is not supported and will lead to a lot of groups having size one, which can still very rarely happen when the border has the correct center on (0, 0).

The group sizes of 2, 3, 4, 5, 6, 7 have the following ratios to appear: 5:5:3:3:1:1.

## Suspicious Liquid
### Properties
This block behaves like other liquids e.g. water or lava but it has a collision box, meaning you can stand on it and can't directly penetrate the liquid.

### Teleportation
If you manage to penetrate the somehow, meaning that you're bathing in the liquid, you get immediately teleported as if you were bathing in the nearest lake. This also happens to other entities and the player if an enderpearl lands on an activated suspicious liquid. If the chunk-distance is equal to two lakes or more, the lake that you are assigned to is chosen at random. Suspicious Liquid only activates and therefore teleports if they are in on of the biomes:
- End Highlands
- End Midlands
- Small End Islands
- End Barrens

### Emergency Lake
I was careful about always generating a suspicious lake where the algorithm expects one. If for any reason there is still no suspicious lake, may it be, because the chunks were generated before my mod was added or because the suspicious lake was removed, an emergency lake will be formed. This will happen if the top block on the chunk at (8, 8) chunk coordinates is not suspicious liquid. It will replace the top block with suspicious liquid and add supporting end stone whereever needed (blocks in minecraft:replaceable).

### Interaction with other liquids
When lava and suspicious liquid meet above bedrock after version 1.0.1, obsidian is formed. Any other interaction results in End Stone.

# How does this mod manage to "know" where to teleport you without generating additional chunks?
Short answer: Through so-called [permutations](https://en.wikipedia.org/wiki/Permutation). Except for world border changes the complexity for every feature of this mod is constant. The code for changing the world border is only triggered when an actual lake teleport happens after a world border change and is still very fast. For the long answer, see [maths.md](maths.md)

# Will this come to neoforge?
Unfortunately I started this project before I knew [Architectury](https://docs.architectury.dev/start) which makes creating mods for both fabric and neoforge a breeze. Adding architectury afterwards is so difficult it's easier to start from scratch.

# Links
[Modrinth](https://modrinth.com/mod/suspicious-lakes/versions)
[Curseforge](https://www.curseforge.com/minecraft/mc-mods/suspicious-lakes/files/all?page=1&pageSize=20&showAlphaFiles=hide)
[this Wiki](/readme.md)
[Maths](/maths.md)
