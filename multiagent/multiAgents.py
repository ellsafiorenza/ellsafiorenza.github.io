# multiAgents.py
# --------------
# Licensing Information:  You are free to use or extend these projects for
# educational purposes provided that (1) you do not distribute or publish
# solutions, (2) you retain this notice, and (3) you provide clear
# attribution to UC Berkeley, including a link to http://ai.berkeley.edu.
#
# Attribution Information: The Pacman AI projects were developed at UC Berkeley.
# The core projects and autograders were primarily created by John DeNero
# (denero@cs.berkeley.edu) and Dan Klein (klein@cs.berkeley.edu).
# Student side autograding was added by Brad Miller, Nick Hay, and
# Pieter Abbeel (pabbeel@cs.berkeley.edu).


from util import manhattanDistance
from game import Directions
import random, util

from game import Agent

class ReflexAgent(Agent):
    """
    A reflex agent chooses an action at each choice point by examining
    its alternatives via a state evaluation function.

    The code below is provided as a guide.  You are welcome to change
    it in any way you see fit, so long as you don't touch our method
    headers.
    """


    def getAction(self, gameState):
        """
        You do not need to change this method, but you're welcome to.

        getAction chooses among the best options according to the evaluation function.

        Just like in the previous project, getAction takes a GameState and returns
        some Directions.X for some X in the set {NORTH, SOUTH, WEST, EAST, STOP}
        """
        # Collect legal moves and child states
        legalMoves = gameState.getLegalActions()

        # Choose one of the best actions
        scores = [self.evaluationFunction(gameState, action) for action in legalMoves]
        bestScore = max(scores)
        bestIndices = [index for index in range(len(scores)) if scores[index] == bestScore]
        chosenIndex = random.choice(bestIndices) # Pick randomly among the best

        "Add more of your code here if you want to"

        return legalMoves[chosenIndex]

    def evaluationFunction(self, currentGameState, action):
        """
        Design a better evaluation function here.

        The evaluation function takes in the current and proposed child
        GameStates (pacman.py) and returns a number, where higher numbers are better.

        The code below extracts some useful information from the state, like the
        remaining food (newFood) and Pacman position after moving (newPos).
        newScaredTimes holds the number of moves that each ghost will remain
        scared because of Pacman having eaten a power pellet.

        Print out these variables to see what you're getting, then combine them
        to create a masterful evaluation function.
        """
        # Useful information you can extract from a GameState (pacman.py)
        childGameState = currentGameState.getPacmanNextState(action)
        newPos = childGameState.getPacmanPosition()
        newFood = childGameState.getFood()
        newGhostStates = childGameState.getGhostStates()
        newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

        "*** YOUR CODE HERE ***"
        newGhostPosition = childGameState.getGhostPositions()

        ghostScore = 0
        closestGhost = min([manhattanDistance(newPos, pos) for pos in newGhostPosition])
        if closestGhost < 2:
            ghostScore = -200

        foodList = newFood.asList()
        foodScore = 1
        if len(foodList) > 0:
            foodPosition = [manhattanDistance(newPos, pos) for pos in foodList]
            foodScore = 1/float(min([10000000000] + foodPosition))

        return (childGameState.getScore() - currentGameState.getScore()) + foodScore + ghostScore

def scoreEvaluationFunction(currentGameState):
    """
    This default evaluation function just returns the score of the state.
    The score is the same one displayed in the Pacman GUI.

    This evaluation function is meant for use with adversarial search agents
    (not reflex agents).
    """
    return currentGameState.getScore()

class MultiAgentSearchAgent(Agent):
    """
    This class provides some common elements to all of your
    multi-agent searchers.  Any methods defined here will be available
    to the MinimaxPacmanAgent, AlphaBetaPacmanAgent & ExpectimaxPacmanAgent.

    You *do not* need to make any changes here, but you can if you want to
    add functionality to all your adversarial search agents.  Please do not
    remove anything, however.

    Note: this is an abstract class: one that should not be instantiated.  It's
    only partially specified, and designed to be extended.  Agent (game.py)
    is another abstract class.
    """

    def __init__(self, evalFn = 'scoreEvaluationFunction', depth = '2'):
        self.index = 0 # Pacman is always agent index 0
        self.evaluationFunction = util.lookup(evalFn, globals())
        self.depth = int(depth)

class MinimaxAgent(MultiAgentSearchAgent):
    """
    Your minimax agent (question 2)
    """

    def getAction(self, gameState):
        """
        Returns the minimax action from the current gameState using self.depth
        and self.evaluationFunction.

        Here are some method calls that might be useful when implementing minimax.

        gameState.getLegalActions(agentIndex):
        Returns a list of legal actions for an agent
        agentIndex=0 means Pacman, ghosts are >= 1

        gameState.getNextState(agentIndex, action):
        Returns the child game state after an agent takes an action

        gameState.getNumAgents():
        Returns the total number of agents in the game

        gameState.isWin():
        Returns whether or not the game state is a winning state

        gameState.isLose():
        Returns whether or not the game state is a losing state
        """
        "*** YOUR CODE HERE ***"
        def maximize(gameState, depth, agentIndex):
            depth -= 1
            if depth < 0 or gameState.isWin() or gameState.isLose():
                return (self.evaluationFunction(gameState), None)
            maxVal = float("-inf")

            for action in gameState.getLegalActions(agentIndex):
                successor = gameState.getNextState(agentIndex, action)
                score = minimize(successor, depth, agentIndex+1)[0]
                if score > maxVal:
                    maxVal = score
                    maxAction = action

            return (maxVal, maxAction)

        def minimize(gameState, depth, agentIndex):
            if depth < 0 or gameState.isWin() or gameState.isLose():
                return (self.evaluationFunction(gameState),None)
            miniVal = float("inf")

            if agentIndex < gameState.getNumAgents()-1:
                evalfunc = minimize
                nextAgent = agentIndex+1
            else:
                evalfunc = maximize
                nextAgent = 0

            for action in gameState.getLegalActions(agentIndex):
                successor = gameState.getNextState(agentIndex, action)
                score = evalfunc(successor, depth, nextAgent)[0]
                if score < miniVal:
                    miniVal = score
                    miniAction = action

            return (miniVal, miniAction)

        return maximize(gameState, self.depth, 0)[1]

class AlphaBetaAgent(MultiAgentSearchAgent):
    """
    Your minimax agent with alpha-beta pruning (question 3)
    """

    def getAction(self, gameState):
        """
        Returns the minimax action using self.depth and self.evaluationFunction
        """
        "*** YOUR CODE HERE ***"
        alfa = -99999999
        beta = 99999999

        def maximize(gameState, depth, alfa, beta):
            v = -9999999999
            numAgent = gameState.getNumAgents()
            numGhost = numAgent - 1

            for act in gameState.getLegalActions(0):
                if numGhost == 0:
                    v = max(v, value(0, gameState.getNextState(0, act), depth + 1, alfa, beta))
                    if v > beta:
                        return v
                    alfa = max(v, alfa)
                else:
                    v = max(v, value(1, gameState.getNextState(0, act), depth, alfa, beta))
                    if v > beta:
                        return v
                    alfa = max(alfa, v)
            return v

        def minimize(gameState, depth, agentIndex, alfa, beta):
            v = 9999999999
            numAgent = gameState.getNumAgents()
            numGhost = numAgent - 1

            for act in gameState.getLegalActions(agentIndex):
                if agentIndex == numGhost:
                    v = min(v, value(0, gameState.getNextState(agentIndex, act), depth + 1, alfa, beta))
                    if v < alfa:
                        return v
                    beta = min(beta,v)
                else:
                    v = min(v, value(agentIndex + 1, gameState.getNextState(agentIndex, act), depth, alfa, beta))
                    if v < alfa:
                        return v
                    beta = min(beta,v)
            return v

        def value(agentIndex, gameState, depth, alfa ,beta):
            if depth == self.depth:
                score = self.evaluationFunction(gameState)
                return score
            if gameState.isWin() or gameState.isLose():
                score = self.evaluationFunction(gameState)
                return score

            numAgent = gameState.getNumAgents()
            numGhost = numAgent - 1
            if agentIndex == 0 or numGhost == 0:
                return maximize(gameState, depth, alfa, beta)
            return minimize(gameState, depth, agentIndex, alfa, beta)

        numAgent = gameState.getNumAgents()
        numGhost = numAgent - 1
        scores = []
        for act in gameState.getLegalActions():
            successor = gameState.getNextState(0, act)
            if numGhost == 0:
                v = value(0, gameState.getNextState(0, act), 1, alfa, beta)
            else:
                v = value(1, gameState.getNextState(0, act), 0, alfa, beta)
            alfa = max(v, alfa)
            scores.append(v)

        bestScore = max(scores)
        bextIndex = [index for index in range(len(scores)) if scores[index] == bestScore]
        return gameState.getLegalActions()[bextIndex[0]]

class ExpectimaxAgent(MultiAgentSearchAgent):
    """
      Your expectimax agent (question 4)
    """

    def getAction(self, gameState):
        """
        Returns the expectimax action using self.depth and self.evaluationFunction

        All ghosts should be modeled as choosing uniformly at random from their
        legal moves.
        """
        "*** YOUR CODE HERE ***"
        if gameState.isWin() or gameState.isLose():
            return Directions.STOP

        num_ghost = gameState.getNumAgents() - 1

        def minimax(state, isPacman, num_agent, depth):
            if state.isWin() or state.isLose():
                return self.evaluationFunction(state)
            if isPacman:
                max_score = float("-inf")
                chosen_action = Directions.STOP

                possible_moves = state.getLegalActions(0)
                for action in possible_moves:
                    possible_state = state.getNextState(0,action)
                    temp = minimax(possible_state, False, num_agent, depth + 1)
                    if temp > max_score:
                        max_score = temp
                        chosen_action = action
                if depth == 0:
                    return chosen_action
                return max_score

            else:
                possible_moves = state.getLegalActions(num_agent)
                possible_states = [state.getNextState(num_agent, action) for action in possible_moves]
                if depth == self.depth and num_agent == num_ghost:
                    return sum([self.evaluationFunction(s) * 1/len(possible_moves) for s in possible_states])
                elif num_agent == num_ghost:
                    return sum([minimax(s, True, 1, depth) for s in possible_states])
                else:
                    return sum([minimax(s, False, num_agent + 1, depth) for s in possible_states])

        return minimax(gameState, True, 1, 0)

def betterEvaluationFunction(currentGameState):
    """
    Your extreme ghost-hunting, pellet-nabbing, food-gobbling, unstoppable
    evaluation function (question 5).

    DESCRIPTION: <write something here so we know what you did>
    """
    "*** YOUR CODE HERE ***"
    score = currentGameState.getScore()
    numFood = currentGameState.getNumFood()
    numCapsules = len(currentGameState.getCapsules())
    newPos = currentGameState.getPacmanPosition()
    newFood = currentGameState.getFood()
    newGhostStates = currentGameState.getGhostStates()
    newScaredTimes = [ghostState.scaredTimer for ghostState in newGhostStates]

    distance = float("inf")
    for food in newFood.asList():
        distance = min(distance, manhattanDistance(newPos, food))

    # find distance of nearest ghost
    badGhostDist = float("inf")
    ghostDist = float("inf")
    for ghost in newGhostStates:
        if ghost.scaredTimer > 0:
            ghostDist = min(ghostDist, manhattanDistance(newPos, ghost.getPosition()))
        else:
            badGhostDist = min(badGhostDist, manhattanDistance(newPos, ghost.getPosition()))

    #if scared ghost is closer to us than bad ghost, and
    #if scared ghost is less than 3 distance away we prioritize eating the ghost.
    if ghostDist < badGhostDist and ghostDist < 3:
        return (100 * ghostDist) - (6 * distance) - (10 * numFood) + (10 * score)

    #If badGhost is less than 3 distance away from pacman, we run away
    if badGhostDist < 3:
        return -10000000

    if currentGameState.getNumFood() == 0:
        return float("inf")

    if badGhostDist != float("inf"):
        ghostDist = badGhostDist
    return ghostDist - (10 * distance) - (100 * numFood) + (100 * score) - (100 * numCapsules)

# Abbreviation
better = betterEvaluationFunction
