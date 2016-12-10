import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


public class YoungManAI extends CKPlayer {

   public YoungManAI(byte player, BoardModel state) {
      super(player, state);
      teamName = "YoungManAI";
   }

   @Override
   public Point getMove(BoardModel state) {
      if (state.gravity == false)
         return nonGravityMove(state);
      else
         return GravityMove(state);
   }

   @Override
   public Point getMove(BoardModel state, int deadline) {
      return getMove(state);
   }

   public Point GravityMove(BoardModel state){
      if (state.spacesLeft == state.width*state.height ){
         int w = (int) Math.ceil((state.width - 1)/ 2.0);
         return new Point(w, 0);
      }
      else 
         return bestSearch(state);
   }

   public Point nonGravityMove(BoardModel state){
      if (state.spacesLeft == state.width*state.height ){ //has the most possibility at the center 
         int h = (int) Math.ceil((state.height - 1) / 2.0); 
         int w = (int) Math.ceil((state.width - 1)/ 2.0);
         return new Point(w, h);
      }
      else 
         return bestSearch(state);
   }

   public Point bestSearch(BoardModel state){
      double startTime = System.currentTimeMillis();
      byte player = 0;
      if ((state.spacesLeft%2) == 0)
         player = 2;
      else 
         player = 1;

      TreeNode<BoardModel> currentNode = new TreeNode<BoardModel> (state);
      currentNode.player = player;
      MaxSearch(currentNode, 0, startTime); // minmax search, max search first
      TreeNode<BoardModel> tempNode = new TreeNode<BoardModel> ();
      for(int i = 0; i < currentNode.children.size(); i++){
         tempNode = currentNode.children.get(i);
         if( tempNode.hVal == currentNode.alpha){
            return tempNode.move;
         }
      }    
      return null;
   }

   public void MaxSearch(TreeNode<BoardModel> stateNode, int depth,  double startime){
      if (depth > 3)
         return;

      byte player = 1;
      if ( depth > 0){
         if(stateNode.player == 1)
            player = 2;
      }
      else
         player = stateNode.player;

      for(int h = 0; h <  stateNode.state.height; h++)   //add all possible moves as its children 
         for(int w = 0; w <  stateNode.state.width; w++){
            if ((System.currentTimeMillis() - startime) >= 5000.0)
               return;

            if (stateNode.state.getSpace(w, h) == 0){
               BoardModel tempState = stateNode.state.placePiece(new Point(w,h), player);
               TreeNode<BoardModel> childNode;
               int goal = checkGoal(tempState, w, h, player);
               if ( goal == Integer.MAX_VALUE)
                  childNode = stateNode.addChild(tempState, goal, 
                        new Point(w,h), stateNode.alpha, 0, player);
               else {
                  childNode = stateNode.addChild(tempState, heuriFunct(tempState, stateNode.player), 
                        new Point(w,h), stateNode.alpha, 0, player);
               }
               if ( stateNode.alpha <  childNode.hVal)
                  stateNode.alpha = childNode.hVal;

            }
         }
   }

   public int heuriFunct(BoardModel state, byte player){
      byte Rivalplayer = 1;
      if (player == 1)
         Rivalplayer = 2;

      int maxGoal = Integer.MIN_VALUE;;
      for(int y = 0; y < state.height; y++)  
         for( int x = 0; x < state.width; x++){
            if (state.getSpace(x, y) == 0){
               int NumOfMyGoal = 0,NumOfRivalGoal = 0;
               NumOfMyGoal += checkGoal(state, x, y, player); // Goal of itself 
               if(NumOfMyGoal == Integer.MAX_VALUE)
                  return NumOfMyGoal;

               NumOfRivalGoal += checkGoal(state, x, y, Rivalplayer); // Goal of rival
               if(NumOfRivalGoal == Integer.MAX_VALUE)
                  return NumOfRivalGoal;


               int goalDifference = NumOfMyGoal - NumOfRivalGoal;
               if ( maxGoal < goalDifference)
                  maxGoal = goalDifference;
            }
         }
      return maxGoal;
   }

   public int checkGoal(BoardModel state, int x, int y, int player){ 
      int numOfGoal = 0;
      byte Rivalplayer = 1;
      if(player ==  1)
         Rivalplayer = 2;

      for(int i = -state.kLength + 1; i < state.kLength; i++){ //goal include horizontal, vertical, left diagonal, right diagonal
         boolean goalH = true, goalV = true, goalL = true, goalR = true;
         int atPathH = 0, atPathV = 0, atPathL = 0, atPathR = 0;
         for (int k = 0; k < state.kLength; k++){ 
            int horizontal = x + i + k, vertical = y + i + k, horizontal2 = x - i + k;

            if ( horizontal >= state.width || horizontal < 0 ) // horizontal
               goalH = false;
            else if ( state.pieces[horizontal][y] == Rivalplayer) 
               goalH = false;
            else if (state.pieces[horizontal][y] == player && goalH == true)
               atPathH++;

            if ( vertical >= state.height || vertical < 0 ) // vertical
               goalV = false;
            else if ( state.pieces[x][vertical] == Rivalplayer) 
               goalV = false;
            else if (state.pieces[x][vertical] == player && goalV == true)
               atPathV++;

            if ( vertical >= state.height || vertical < 0 || // left diagonal
                  horizontal >= state.width || horizontal < 0  )
               goalL = false;
            else if ( state.pieces[horizontal][vertical] == Rivalplayer)
               goalL = false;
            else if (state.pieces[horizontal][vertical] == player && goalL == true)
               atPathL++;

            if ( vertical >= state.height || vertical < 0 || 
                  horizontal2 >= state.width || horizontal2 < 0  ) // right diagonal
               goalR = false;
            else if ( state.pieces[horizontal2][vertical] == Rivalplayer)
               goalR = false;
            else if(state.pieces[horizontal2][vertical] == player && goalR == true)
               atPathR++;

         }

         if (goalH == true){
            numOfGoal++;
            if( state.kLength - atPathH == 0 ){
               numOfGoal = Integer.MAX_VALUE;
               return numOfGoal;
            }

         }

         if (goalV == true){
            numOfGoal++;
            if( state.kLength - atPathV == 0 ){
               numOfGoal = Integer.MAX_VALUE;
               return numOfGoal;
            }
         }

         if (goalL == true){
            numOfGoal++;  
            if( state.kLength - atPathL == 0 ){
               numOfGoal = Integer.MAX_VALUE;
               return numOfGoal;
            }
         }

         if (goalR == true){
            numOfGoal++;
            if( state.kLength - atPathR == 0 ){
               numOfGoal = Integer.MAX_VALUE;
               return numOfGoal;
            }
         }
      }
      return numOfGoal;
   }
}


class TreeNode<T>
{
   T state;
   int alpha;
   int beta;
   int hVal = 0;
   byte player;
   Point move;
   TreeNode<T> parent;
   List<TreeNode<T>> children;

   public TreeNode() {
      this.state = null;
      this.alpha = Integer.MIN_VALUE;
      this.beta = Integer.MAX_VALUE;
      this.hVal = 0;
      this.children = new ArrayList<TreeNode<T>>();
   }

   public TreeNode(T rootData) {
      this.state = rootData;
      this.alpha = Integer.MIN_VALUE;
      this.beta = Integer.MAX_VALUE;
      this.children = new ArrayList<TreeNode<T>>();
   }

   public TreeNode<T> addChild(T child, int functionVal, Point move, int alpha, int beta, byte player) {
      TreeNode<T> childNode = new TreeNode<T>(child);
      childNode.parent = this;
      childNode.player = player;
      childNode.hVal = functionVal;
      childNode.move = move;
      if (alpha != 0)
         childNode.alpha = alpha;
      if (beta != 0)
         childNode.beta = beta;
      this.children.add(childNode);
      return childNode;
   }
}



