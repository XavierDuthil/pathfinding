
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class Pathfinding
{
    SPJFrame frame;
    int maxPenalty = 0;
            
    public Pathfinding(SPJFrame frame)
    {
        this.frame = frame;
    }
            
    public ArrayList<String> findShortestPath()
    {
        String start = frame.getSourceNode();
        String goal = frame.getTargetNode();
        ArrayList<String> closedNodes = new ArrayList<>();
        HashMap<String, String> parentNode = new HashMap<>();
        ArrayList<String> openNodes = new ArrayList<>();
        openNodes.add(start);

        // Cost from start along best known path.
        HashMap<String, Double> g_score = new HashMap<>();    
        g_score.put(start, 0.0);
        
        // Estimated total cost from start to goal through y.
        HashMap<String, Double> f_score = new HashMap<>();
        f_score.put(start, g_score.get(start) + heuristicCostEstimate(start, goal));
        
        // Penalités dans ce dictionnaire
        HashMap<String, Integer> penalties = new HashMap<>();
        
        System.out.println("Distance Manhattan totale : " +(manhattanDistBetween(start, goal)/distBetween("v0", "v1")+2));
        System.out.println("Distance diagonale totale : " +(manhattanDistBetween(start, goal)/distBetween("v0", "v1")+2)/2);
        
        while (!openNodes.isEmpty())
        {
            String current = getClosestNodeWithLowestPenalty(openNodes, penalties, f_score);
            
            if (current.equals(goal))
                return reconstructPath(parentNode, goal);
            
            // Deletes the current node from openNodes and adds it to closedNodes
            openNodes.remove(current);
            closedNodes.add(current);
            
            Collection<String> neighbors = new ArrayList<>();
            neighbors = frame.g.getNeighbors(current);

            for (String neighbor : neighbors)
            {                
                double tentative_g_score = g_score.get(current) + distBetween(current,neighbor);
                double tentative_f_score = heuristicCostEstimate(neighbor, goal);
                /*
                if (!openNodes.contains(neighbor) || tentative_g_score < g_score.get(neighbor))
                {
                    double tentative_f_score = heuristicCostEstimate(neighbor, goal);
                            
                    parentNode.put(neighbor, current);  
                    g_score.put(neighbor, tentative_g_score);
                    f_score.put(neighbor, tentative_f_score);

                    if (!openNodes.contains(neighbor))
                        openNodes.add(neighbor);

                    frame.vcolor.put(neighbor, Color.blue);
                    frame.repaint();
                    try {Thread.sleep(20);} catch (Exception e){}
                 }*/
                
                // if already visited but path better now : update
                if (openNodes.contains(neighbor) && g_score.get(neighbor) > tentative_g_score)
                {
                    openNodes.remove(neighbor);
                    
                    int penalty = 0;
                    // Ajout de pénalité s'il s'agit d'un pas en arrière
                    if (tentative_f_score > f_score.get(current))
                    {
                        
                        if (penalties.containsKey(current))
                            penalty = penalties.get(current) + 1;
                        else
                            penalty = 1;
                    }
                    
                    else
                    {
                        // On met à jour la pénalité vu qu'il s'agit d'un meilleur chemin
                        if (penalties.containsKey(current))
                            penalty = penalties.get(current);
                        else
                            penalty = 0;
                    }
                    
                    if (penalty > maxPenalty)
                        maxPenalty = penalty;
                    penalties.put(neighbor, penalty);
                    frame.vcolor.put(neighbor, colorPenalty(penalty));
                }
                
                // if already visited but path better now : update
                if (closedNodes.contains(neighbor) && g_score.get(neighbor) > tentative_g_score)
                {
                    closedNodes.remove(neighbor);
                    int penalty = 0;
                    // Ajout de pénalité s'il s'agit d'un pas en arrière
                    if (tentative_f_score > f_score.get(current))
                    {
                        
                        if (penalties.containsKey(current))
                            penalty = penalties.get(current) + 1;
                        else
                            penalty = 1;
                    }
                    
                    else
                    {
                        // On met à jour la pénalité vu qu'il s'agit d'un meilleur chemin
                        if (penalties.containsKey(current))
                            penalty = penalties.get(current);
                        else
                            penalty = 0;
                    }
                    if (penalty > maxPenalty)
                        maxPenalty = penalty;
                    
                    penalties.put(neighbor, penalty);
                    frame.vcolor.put(neighbor, colorPenalty(penalty));
                }
                
                // new value to register
                if (!openNodes.contains(neighbor) && !closedNodes.contains(neighbor))
                {
                    g_score.put(neighbor, tentative_g_score);
                    openNodes.add(neighbor);
                    f_score.put(neighbor, tentative_f_score);
                    parentNode.put(neighbor, current);
                    
                    if (neighbor.equals(goal))
                        return reconstructPath(parentNode, goal);
                    
                    int penalty = 0;
                    if (penalties.containsKey(current))
                        penalty = penalties.get(current);
                    
                    // Ajout de pénalité s'il s'agit d'un pas en arrière
                    if (tentative_f_score > f_score.get(current))
                    {
                        if (penalties.containsKey(current))
                        {
                            penalty = penalties.get(current) + 1;
                            if (penalty > maxPenalty)
                                maxPenalty = penalty;
                        }
                        else
                            penalty = 1;
                        //frame.vcolor.put(neighbor, new Color(penalty*50, 0, 0));
                    }
                    
                    penalties.put(neighbor, penalty);
                    frame.vcolor.put(neighbor, colorPenalty(penalty));
                    frame.repaint();
                    try {Thread.sleep(8);} catch (Exception e){}
                    
                    
                }
            }
        }
        return null;
    }
        
    public double heuristicCostEstimate(String a, String b)
    {
        //return manhattanDistBetween(a, b);
        
        // Diagonal shortcut
        Point2D startPoint = frame.coordmap.get(a);
        Point2D goalPoint =  frame.coordmap.get(b);
        double xDistance = Math.abs(startPoint.getX()-goalPoint.getX());
        double yDistance = Math.abs(startPoint.getY()-goalPoint.getY());
        if (xDistance > yDistance)
             return 14*yDistance + 10*(xDistance-yDistance);
        else
             return 14*xDistance + 10*(yDistance-xDistance);
    }
    
    public ArrayList<String> reconstructPath(HashMap<String, String> parentNode, String current)
    {
        ArrayList<String> path = new ArrayList<>();
        path.add(current);
        while (parentNode.containsKey(current))
        {
            current = parentNode.get(current);
            path.add(current);
        }

        for (String node : path)
        {
            frame.vcolor.put(node, Color.green);
            frame.repaint();
        }
        
        System.out.println("Distance parcourue : " +path.size());
        System.out.println("Max penalty : " +maxPenalty);
        return path;
    }
    
    public double distBetween(String a, String b)
    {
        Point2D aPoint = frame.coordmap.get(a);
        Point2D bPoint = frame.coordmap.get(b);
        return aPoint.distance(bPoint);
    }
    
    public String getClosestNodeWithLowestPenalty(ArrayList<String> openNodes, HashMap<String, Integer> penalties, HashMap<String, Double> f_score)
    {
        String closestNode = null;
        double min = -1;

        int currentPenalty = 0;
        boolean found = false;
        
        while (!found && currentPenalty <= maxPenalty)
        {
            for (String node : openNodes)
            {
                if (currentPenalty == 0)
                {
                    if (penalties.containsKey(node) && penalties.get(node) > 0)
                        continue;
                }
                else
                    if (penalties.get(node) > currentPenalty)
                        continue;

                found = true;
                if (f_score.containsKey(node))
                {
                    double nodeScore = f_score.get(node);
                    if (min < 0 || nodeScore < min)
                    {
                        min = nodeScore;
                        closestNode = node;
                    }
                }
            }
            currentPenalty ++;
        }
        return closestNode;
    }
    
    public double manhattanDistBetween(String a, String b)
    {
        Point2D startPoint = frame.coordmap.get(a);
        Point2D goalPoint =  frame.coordmap.get(b);
        double xDiff = Math.abs(goalPoint.getX() - startPoint.getX());
        double yDiff = Math.abs(goalPoint.getY() - startPoint.getY());
        double estimatedCost =  xDiff + yDiff;
        return estimatedCost;
    }
    
    public Color colorPenalty(int penalty)
    {
        Color color;
        if (penalty <= 5)
            color = new Color(penalty*50,0,255);
        
        else if (penalty <= 10)
        {
            color = new Color(255,0,255-(penalty-5)*50);
        }
        
        else if (penalty <= 15)
            color = new Color(255-(penalty-10)*50,0,255-(penalty-10)*50);
        
         else
            color = Color.BLACK;
        
        return color;
    }
}
