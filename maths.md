## Translation of config to math variables

| ModMenu | config          | this document                      |
| ------- | --------------- | ---------------------------------- |
|         | nrLakes         | n                                  |
|         | powerDistance   | e                                  |
|         | cycleWeights    | relates to  $n1, \ldots, n_k$      |
|         | minimumDistance | d                                  |
|         | factsPhi        | list of factors of $\phi(n)=n - 1$ |


*) cycleWeights define weights for the length of each cycle, that means the first entry is the weight of the 1-cycle, the second entry is the weight of the 2-cycle and so on

## Fixed things
### pi
$\pi = (1)(2)(3)...(n_1)$

$\circ (n_1 + 1, n_1 + 2)(n_1 + 3, n_1 + 4)...(n_1 + n_2 - 1, n_1 + n_2)$

$\circ ...$

$\circ (n_1 + ... + n_k - k + 1 ... n_1 + ... + n_k)$

Described in words: A permutation with each element has defined probabilities of being inside a cycle of special length. The defined probabilities are configurable.

calculated lazy

### n
is a large prime

calculated from the world border

### c[.]
A bijection that maps $\mathbb N \to \mathbb Z \times \mathbb Z$

calculated lazy

### pos[., .]
The base chunk positions of the lakes based on x and y integer coordinates. An example for this would be:

$f(c) = d \cdot |c|^e$ if $c > 0$

$f(0) = 0$ and

$f(c) = -d \cdot |c|^e$ if $c < 0$

$pos[0, 0] = null$

$pos[x, y] = [f(x), f(y)]$

d is configurable

calculated lazy

### o
The integer since when the grid positions of c[o + 1] becomes safe (not inside the biome minecraft:the_end)

## Seed-dependent things
### g
1. Yield a random number g between 2 and n - 1 from the seed
2. Find out, if g is a primitive root mod n (follow [this](https://en.wikipedia.org/wiki/Primitive_root_modulo_n#Finding_primitive_roots))
3. If yes, return g
4. continue with 1.

This helps making the cycles randomly distributed, because $\pi$ isn't random at all. Credits to the idea: [here](https://stackoverflow.com/questions/32357710/efficient-way-to-generate-a-seemingly-random-permutation-from-a-very-large-set-w/47437819#47437819)

calculated eagerly

### off[., .]

off[0, 0] = null

usually

off[x, y] = [rand(pos[x - 1, y].x, pos[x + 1, y].x), rand(pos[x, y - 1].y, pos[x, y + 1])]

if off tries to get an unsafe position, it will instead use x or y correspondingly

This helps placing the lakes a little bit more random so they doesn't appear to be in a grid.

calculated lazy

## calculating the aim lake
### calculate the position of a lake
with grid coordinates x, y, the position of the lake is
$pos[x, y] + off[x, y]$
### calculate the nearest lake
1. get the nearest grid coordinates x, y by reversing the f defined in pos[., .]
2. iterate of each of the neighbours of (x, y), including diagonal neighbours and (x, y):
2a. calculate the position of the lake at the iterating position and calculate the distance to the current chunk
2b. the "winning" (x', y') coordinates is the result
### calculate the next lake
1. calculate the nearest lake to be at grid coordinates (x, y)
2. calculate $[x', y'] = c[g^{-1} \cdot (\pi \circ (g \cdot (c^{-1}[x, y] - o))) + o]$
3. calculate the position of the lake at grid coordinate (x', y') and return this
