# What does this mod do?
It adds a new structure "suspicious lake" to the end dimension. When you accomplish to bath in the solid liquid or throw an ender pearl into it it will teleport you to the next lake. This lake can be anywhere in the end dimension, although they accumulate near the spawn (except for the biome "The End"). The Linkage always forms a loop, meaning, if you use the lake repeatedly, you will eventually land back where you started. These "groups" can have at maximum 6 members. When there is no lake found, an "emergency lake" is spawned, replacing one block with the new fluid added and if needed some support so it doesn't spill over. The fluid only activates in the correct biomes, meaning it doesn't do anything when in the wrong biome. You can however, pick the fluid up with a bucket and use it somewhere else. You won't return to the point where you started however.

# How does this mod manage to "know" where to teleport you without generating additional chunks?
Short answer: Through so-called [permutations](https://en.wikipedia.org/wiki/Permutation). Except for world border changes the complexity for every feature of this mod is constant. For the long answer, see [maths.md](maths.md)

# Will this come to neoforge?
Unfortunately I started this project before I knew [Architectury](https://docs.architectury.dev/start) which makes creating mods for both fabric and neoforge a breeze. Adding architectury afterwards is so difficult it's easier to start from scratch.
