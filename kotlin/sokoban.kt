import pt.isel.canvas.*

/**
 * The width scaling factor for the maze, in pixels.
 */
const val MAZE_WIDTH_SCALING_FACTOR = 39

/**
 * The height scaling factor for the maze, in pixels.
 */
const val MAZE_HEIGHT_SCALING_FACTOR = 53

/**
The vertical offset for the maze, in pixels.
 */
const val MAZE_VERTICAL_OFFSET = 50

/**
 * The width of the arena, in pixels.
 */
const val arenaWidth = 18 * MAZE_WIDTH_SCALING_FACTOR

/**
 * The height of the arena, in pixels.
 */
const val arenaHeight = 15 * MAZE_HEIGHT_SCALING_FACTOR

/**
 * Load the levels from the specified file.
*/

val levels = loadLevels("src/main/resources/Classic.txt")

/**
 * Draws an object on a given canvas using the specified sprite name and position.
 * the +1 fixes the one pixel off bug
 *
 * @param arena The canvas to draw on.
 * @param name The name of the sprite to use for the object.
 * @param pos The position of the object on the canvas.
 * @param dx The x-offset to apply to the position when drawing the object.
 * @param dy The y-offset to apply to the position when drawing the object.
 */
fun drawObject(arena: Canvas, name: String, pos: Position, dx: Int, dy: Int) {
    val sprite = Sprites[name] ?: throw IllegalArgumentException("Sprite not found: $name")
    arena.drawImage(sprite, (pos.col * MAZE_WIDTH_SCALING_FACTOR) + dx, (pos.line * MAZE_HEIGHT_SCALING_FACTOR) + dy, MAZE_WIDTH_SCALING_FACTOR + 1, MAZE_HEIGHT_SCALING_FACTOR + 1)
}


/**
 * Returns a new `Position` that is obtained by adding the given `Direction` to this `Position` with the plus sign.
 *
 * @param dir The `Direction` to add to this `Position`.
 * @return A new `Position` with the updated column and line numbers.
 */

operator fun Position.plus(dir: Direction) = Position(col + dir.dx ,line + dir.dy )

/**
 * Calculates the top-left coordinates of a maze, given its dimensions and the dimensions of the canvas it is to be drawn on,
 * such that the maze is centered on the canvas.
 *
 * @param mazeWidth The width of the maze.
 * @param mazeHeight The height of the maze.
 * @param canvasWidth The width of the canvas.
 * @param canvasHeight The height of the canvas.
 * @return A pair of integers representing the top-left coordinates of the maze.
 */

fun calculateMazeTopLeftCoordinates(mazeWidth: Int, mazeHeight: Int, canvasWidth: Int, canvasHeight: Int): Pair<Int, Int> {
    val x = (canvasWidth - mazeWidth) / 2
    val y = (canvasHeight - mazeHeight) / 2
    return Pair(x - (mazeWidth * MAZE_WIDTH_SCALING_FACTOR) / 2, y - ((mazeHeight * MAZE_HEIGHT_SCALING_FACTOR) - MAZE_VERTICAL_OFFSET) / 2)
}

//--------------------Man--------------------

/**
 * Represents a man on a grid.
 *
 * @param pos The current position of the man.
 * @param dir The current direction the man is facing.
 * @param push Whether the man is currently pushing an object.
 */

data class Man(val pos: Position,
               val dir: Direction = Direction.Down,
               val push: Boolean = false)

/**
 * Returns whether the man can move to the specified position, given the game state and his future position.
 *
 * @param futurepos Man Target Position .
 * @param dir The direction the man is moving in.
 * @param game The current game state.
 * @return `true` if the man can move to the specified position, `false` otherwise.
 */

fun isManInLimits(futurepos: Position, dir: Direction, game: Game): Boolean {
    val boxPositionIfMoved = futurepos + dir
    return if (futurepos in game.boxes.positions) boxPositionIfMoved !in game.walls.positions && boxPositionIfMoved !in  game.boxes.positions
    else futurepos !in game.walls.positions
}
/**
 * Returns whether the man can push a box in the specified direction, given the game state.
 *
 * @param manPos The position of the man.
 * @param game The current game state.
 * @param dir The direction the man is trying to push the box in.
 * @return `true` if the man can push the box in the specified direction, `false` otherwise.
 */
fun canPush(manPos: Position, game: Game, dir: Direction): Boolean {
    // Check if the man is currently standing on a box.
    val isManOnBox = manPos in game.boxes.positions
    // Calculate the target position for the box based on the man's position and the direction being pushed.
    val targetPos = manPos + dir
    // Check if the target position is a valid location for the box to be pushed to.
    val isTargetValid = targetPos !in game.boxes.positions && targetPos !in game.walls.positions
    return isManOnBox && isTargetValid
}

/**
 * Moves the man to the specified position, if it is a legal move. Otherwise, returns the man's current position.
 *
 * @param dir The direction the man is trying to move in.
 * @param game The current game state.
 * @return The man's new position, or the man's current position if the move is illegal.
 */
fun Man.move(dir: Direction, game: Game): Man{
    return if (isManInLimits(pos + dir, dir, game)) Man(pos + dir, dir, canPush(pos + dir, game, dir))
    else Man(this.pos, dir)
}

/**
 * Draws the man on the canvas, given the current game state.
 *
 * @param arena The canvas to draw the man on.
 * @param currentMove The current move number of the man. Used to animate the man's walking motion.
 * @param still Whether the man is standing still or not.
 * @param dx The horizontal offset to draw the man at.
 * @param dy The vertical offset to draw the man at.
 */
fun Man.draw(arena: Canvas, currentMove: Int, still: Boolean = false, dx: Int, dy: Int,boxes: List<Position>) {
    //Is player next to a box?
    val nextToABox = pos + dir in boxes
    // Determine the sprite to use based on the man's current state.
    val sprite = when {
        !still && !nextToABox -> "${dir.name}${currentMove % 2}"
        !still && nextToABox -> "${dir.name}${currentMove % 2}P"
        still && !nextToABox -> "${dir.name}S"
        else -> "${dir.name}PS"
    }
    // Draw the man on the canvas using the appropriate sprite.
    drawObject(arena, sprite, pos, dx, dy)
}

//--------------------Box--------------------
/**
 * Data class representing the boxes in the game
 * @param positions: list of positions of the boxes
 */
data class Boxes(val positions: List<Position>) {

    /**
     * Moves a box in a specified direction
     * @param dir: direction to move the box
     * @param index: index of the box to be moved in the Positions list
     * @param boxesPositions: list of positions of all the boxes
     */
    fun boxMove(dir: Direction, index: Int, boxesPositions: List<Position>) = Boxes(boxesPositions.map { if (it == boxesPositions[index]) boxesPositions[index] + dir else it })

    /**
     * Draws the boxes on the arena
     * @param arena: canvas on which the boxes are drawn
     * @param targetsPositions: list of positions of the targets
     * @param dx: x offset for drawing the boxes
     * @param dy: y offset for drawing the boxes
     */
    fun draw(arena: Canvas, targetsPositions: List<Position>, dx: Int, dy: Int) {
        positions.forEach {
            if (it in targetsPositions) drawObject(arena, "BoxOnTarget", it, dx, dy)
            else drawObject(arena, "Box", it, dx, dy)
        }
    }
}


/**
 * Data class representing the targets in the game
 * @param positions: list of positions of the targets
 */
data class Target(val positions: List<Position>) {

    /**
     * Draws the targets on the arena
     * @param arena: canvas on which the targets are drawn
     * @param dx: x offset for drawing the targets
     * @param dy: y offset for drawing the targets
     */
    fun draw(arena: Canvas, dx: Int, dy: Int) =  positions.forEach { drawObject(arena, "Target", it, dx, dy) }
}

/**
 * Data class representing the walls in the game
 * @param positions: list of positions of the walls
 */
data class Walls(val positions: List<Position>) {

    /**
     * Draws the walls on the arena
     * @param arena: canvas on which the walls are drawn
     * @param dx: x offset for drawing the walls
     * @param dy: y offset for drawing the walls
     */
    fun draw(arena: Canvas, dx: Int, dy: Int) =  positions.forEach { drawObject(arena, "Wall", it, dx, dy) }
}


const val ARENA_BOTTOM_PADDING = 15 // padding for bottom of arena
const val ARENA_LINE_THICKNESS = 100 // thickness of line at bottom of arena
const val LEVEL_TEXT_X_POSITION = 50 // x position for level text
const val MOVES_TEXT_X_POSITION = 250 // x position for moves text


/**
 * Data class representing the game state
 * @param level: current level of the game
 * @param man: position of the man in the game
 * @param walls: positions of the walls in the game
 * @param boxes: positions of the boxes in the game
 * @param targets: positions of the targets in the game
 * @param step: variable used to determine if the man is using the right leg or the left leg
 * @param legalMove: number of legal moves made in the game
 * @param stackMoves: list of previous game states
 */
data class Game(
    val level: Int = 0,
    val man: Man = Man(levels[level].positionOfType(Type.MAN)), // man original position
    val walls: Walls = Walls(levels[level].positionsOfType(Type.WALL)),
    val boxes: Boxes = Boxes(levels[level].positionsOfType(Type.BOX)), // list of original box positions
    val targets: Target = Target(levels[level].positionsOfType(Type.TARGET)),
    val step: Int = 0,
    val legalMove: Int = 0,
    val stackMoves: List<Game> = emptyList()
) {

    /**
     * Draws the game state on the arena
     * @param arena: canvas on which the game is drawn
     * @param still: boolean indicating if the man is still in progress or not
     * @param game: current game state
     */
    fun draw(arena: Canvas, still: Boolean = false, game: Game) {
        // Clear the canvas.
        arena.erase()

        // Calculate the top-left coordinates of the centered maze.
        val dx = calculateMazeTopLeftCoordinates(levels[game.level].width,levels[game.level].height,arenaHeight, arenaWidth ).first
        val dy = calculateMazeTopLeftCoordinates(levels[game.level].width,levels[game.level].height,arenaHeight,arenaWidth).second

        // Draw the targets, walls, man, and boxes on the canvas.
        targets.draw(arena, dx, dy)
        walls.draw(arena, dx, dy)
        man.draw(arena, step, still, dx, dy, game.boxes.positions)
        boxes.draw(arena, game.targets.positions, dx, dy)

        // Draw a horizontal line at the bottom of the arena.
        arena.drawLine(0,  arena.height, arena.width, arena.height, color = CYAN, thickness = ARENA_LINE_THICKNESS)

        // Display the current level and number of moves made in the game.
        arena.drawText(LEVEL_TEXT_X_POSITION, arena.height - ARENA_BOTTOM_PADDING, "Level: ${game.level+1}")
        arena.drawText(arena.width - MOVES_TEXT_X_POSITION, arena.height - ARENA_BOTTOM_PADDING, "Moves: ${game.legalMove}")
    }
    
    /**
     * Increase the level of the game by a specified number.
     * The level is kept within the valid range of 0 to the number of levels.
     *
     * @param num the number to add to the current level
     * @return the new level of the game, which is within the valid range of 0 to the number of levels
     
     */
    fun changeLevel(num: Int):Int{
            val nextLevel = level + num
            // If the level is less than 0, set it to 0
            if (nextLevel < 0) {
                return 0
            }
            // If the level is greater than the number of levels, set it to the number of levels
            if (nextLevel > levels.size-1) {
                return levels.size-1
            }
    
            return nextLevel
        }
}


/**
 * An enum representing the four cardinal directions (up, down, left, and right).
 *
 * @property dx The horizontal movement associated with this direction. A positive value indicates movement to the right, and a negative value indicates movement to the left.
 * @property dy The vertical movement associated with this direction. A positive value indicates movement down, and a negative value indicates movement up.
 */

enum class Direction( val dx: Int = 0, val dy: Int = 0) {
    UP(0, -1),
    Down(0, 1),
    Left(-1,0),
    Right(1,0)
}

/**
 * This function checks if all the boxes in the game are on top of targets.
 *
 * @param game The game state to check.
 * @return true if all the boxes are on targets, false otherwise.
 */
fun allBoxesInTargets(game: Game): Boolean {
    // Check if the sizes of the lists of box positions and target positions are equal,
    // and if all the elements in both lists are contained in each other, ignoring the order.
    return  game.boxes.positions.size == game.targets.positions.size &&
            game.boxes.positions.containsAll(game.targets.positions) &&
            game.targets.positions.containsAll(game.boxes.positions)
}

/**
 * The main function for the game. It sets up the canvas, handles user input, and updates the game state.
 */
fun main() {
    // Initialize the game.
    var game = Game()
    // Create the canvas.
    val arena = Canvas(800, 800)
    // Draw the initial state of the game on the canvas.
    game.draw(arena, game = game)

    // Set up the event handler for key press events.
    onStart {
        arena.onKeyPressed { k ->

            // Check if all boxes are on targets.
            if (!allBoxesInTargets(game)) {
                // Determine the direction of the move based on the key pressed.
                val direction = when (k.text) {
                    "↓" -> Direction.Down
                    "↑" -> Direction.UP
                    "→" -> Direction.Right
                    "←" -> Direction.Left
                    else -> null
                }
                // If a valid direction was specified, update the game state.
                if (direction != null) {

                    // Calculate the new position of the man after the move.
                    val newMan = game.man.move(direction, game)

                    // If the man's position has changed, update the game state.
                    if (newMan.pos != game.man.pos) {
                        // If the man is pushing a box, move both the man and the box. Otherwise, just move the man.
                        game =
                            if (newMan.push)
                                Game(level = game.level,
                                     man = newMan,
                                     boxes = game.boxes.boxMove(
                                         direction,
                                         game.boxes.positions.indexOf(newMan.pos),
                                         game.boxes.positions
                                     ),
                                     step = game.step + 1,
                                     legalMove = game.legalMove + 1,
                                     stackMoves = game.stackMoves + game)
                            else
                                Game(level = game.level,
                                     man = newMan,
                                     boxes = game.boxes,
                                     step = game.step + 1,
                                     legalMove=game.legalMove + 1,
                                     stackMoves = game.stackMoves + game)
                    }
                    // If the man's position has not changed, update the game state to reflect the man animation.
                    else {
                        game =
                            if (newMan.push)
                                Game(level = game.level,
                                     man = newMan,
                                     boxes = game.boxes.boxMove(
                                         direction,
                                         game.boxes.positions.indexOf(newMan.pos),
                                         game.boxes.positions
                                     ),
                                     legalMove = game.legalMove,
                                     stackMoves = game.stackMoves)
                            else
                                Game(level = game.level,
                                     man = newMan,
                                     boxes = game.boxes,
                                     step = game.step + 1,
                                     legalMove=game.legalMove,
                                     stackMoves = game.stackMoves)
                    }
                    // Redraw the game on the canvas.
                    game.draw(arena, game = game)
                }

                // Handle other special keys.
                when (k.text) {
                    "R" -> {
                        // Reset the game to the current level.
                        game = Game(level = game.level)
                    }

                    "/" -> {
                        // Go to the previous level.
                        game = Game(level = game.changeLevel(-1))
                    }

                    "N" -> {
                        // Go to the next level.
                        game = Game(level = game.changeLevel(1))
                    }

                    "⌫" -> {
                        // Undo the last move.
                        if (game.stackMoves.isNotEmpty()) {
                            game = game.stackMoves.last()
                            game.stackMoves.dropLast(1) }
                    }
                }

                // Redraw the game on the canvas.
                game.draw(arena, game = game)
            }


            // If all boxes are on targets, stop updating the game.
            else {
                game =
                    Game(level = game.level,
                         man = game.man,
                         boxes = game.boxes,
                         step = game.step,
                         legalMove = game.legalMove)

                if (k.text == "␣") {
                    // Go to the next level when the space bar is pressed.
                    game = Game(level = game.changeLevel(1))
                    game.draw(arena, game = game)
                }
            }
        }

        /**
         * This function is called every 500 milliseconds to stop the man.
         */
        arena.onTimeProgress(500) {
            game.draw(arena, true, game)
        }
    }
    onFinish {}
}
