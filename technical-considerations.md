## Why don't I use the vanilla structure or feature placement logic?
I need a way to turn the position of a lake into a number and back. While you can turn every 2d point into a number (see [Pairing function](https://en.wikipedia.org/wiki/Pairing_function) although I used a custom one because of my requirements), there is no constant way of turning a number into a position where a generated feature or structure is guaranteed to spawn, at least not if we need to catch every such placement.

## Why don't I just generate random numbers for the grid coordinates and save them then?
I'd like to have the linkage dependant on the seed. Because of technical limitations the linkage unfortunately also depends on world border, but for a given world border, every world with the same seed will have the same linkage between its suspicious lakes. It should neither depend on the order of using and generating the lakes, nor on the seed.

## Why don't I just generate the random coordinates depending on the seed then?
Because it would lead to not unique linkage. If you enter a lake A and have this lake linked to B and then back, if you first enter the lake B you wouldn't automatically go to lake A.
