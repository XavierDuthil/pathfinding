import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractModalGraphMouse;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.TransformerUtils;

/**
 *
 * @author V. Levorato
 */
public class SPJFrame extends javax.swing.JFrame implements ActionListener {

    public Graph<String,Number> g;
    public VisualizationViewer vv;
    
    public HashMap<String,Color> vcolor=new HashMap();
    
    public Map<String,Point2D> coordmap = new HashMap<String,Point2D>();
    
    JButton launchButton;
    Pathfinding pathfinding;
    
    int size=1000; //taille de la grille (en pixels)
    double oamount=0.2; //proportion d'obstacles
    
    class VertexColorTransformer implements Transformer<Object,Color> {

        //palette de couleur
        HashMap<Object,Color> colortable;
        
        public VertexColorTransformer(HashMap ctable){ colortable=ctable; }

        @Override
        public Color transform(Object v) {
            return colortable.get(v);
        }
    }
    

    /** Creates new form TPJFrame */
    public SPJFrame() throws IOException
    {
        /** NE PAS MODIFIER LE CODE **/
        initComponents();
        
        //déclaration d'un graphe orienté (liste d'adjacence)
        g = new UndirectedSparseGraph();
        
        //déclaration d'une table avec la table des couleurs des noeuds
        vcolor=new HashMap();
     
        /** définition du rendu visuel du graphe **/
        //définition de l'algorithme de rendu (Grille 2D)
        Dimension preferredSize = new Dimension(size,size);
        coordmap = new HashMap<String,Point2D>();
        Transformer<String,Point2D> vlf = TransformerUtils.mapTransformer(coordmap);
        g= generateVertexGrid(coordmap, preferredSize, 25);
        Layout<String,Number> gridLayout = new StaticLayout<String,Number>(g, vlf, preferredSize);
        vv = new VisualizationViewer(gridLayout); 
       
        System.out.println("Nombre de noeuds:"+coordmap.size());
        
        //définit la couleur par défaut
        for(String v : g.getVertices())
            vcolor.put(v, Color.white);
        
        //définit le noeud source et cible
        vcolor.put(getSourceNode(), Color.green);
        vcolor.put(getTargetNode(), Color.red);
        
        setObstacles();
        
        //définition de listeners prédéfinis pour la souris
        final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        vv.setGraphMouse(graphMouse);
        
        //gestion de la couleur des sommets
        vv.getRenderContext().setVertexFillPaintTransformer(new VertexColorTransformer(vcolor));

        
        //fond en blanc
        vv.setBackground(Color.white);
        	
        
        //ajout de la visualisation du graphe au jFrame principal
        this.getContentPane().add(vv);
        this.pack();
        /** NE PAS MODIFIER LE CODE **/
       
        //Trouve le plus court chemin 
        //Votre méthode à insérer
        //this.setLayout(new FlowLayout());
        launchButton = new JButton("Lancer");
        launchButton.addActionListener(this);
        //this.add(launchButton);
        this.setSize(1100, 1030);
        
        final Pathfinding pathfinding = new Pathfinding(this);
        new Thread() {  
            public void run() {
                try{
                Thread.sleep(1000);
                } catch (Exception e){}
                
                if (pathfinding.findShortestPath() == null)
                    System.out.println("Aucun chemin possible");
            }  
        }.start();  
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        
    } 
     
    
    
    //NE PAS MODIFIER APRES CETTE LIGNE
    
    private int getDistance(String v1, String v2)
    {
        int d=0;
        Point2D p1=coordmap.get(v1);
        Point2D p2=coordmap.get(v2);
        d+=Math.abs(p1.getX()-p2.getX());
        d+=Math.abs(p1.getY()-p2.getY());
        
        return d;
    }
    
    
    public String getSourceNode()
    {
        return "v0";
    }
    
    public String getTargetNode()
    {
        String v="v";
        v+=(coordmap.size()-1);
        return v;
    }
    
    private void setObstacles()
    {
        String start=getSourceNode();
        String end=getTargetNode();
        
        for(String v: g.getVertices())
            if(Math.random()<oamount && !v.equals(start) && !v.equals(end))
            {
                vcolor.put(v, Color.gray);
                
                // Delete all edges to these points
                Collection<Number> edges = new ArrayList<>();
                edges = g.getIncidentEdges(v);
                for (Number edge : edges)
                {
                    g.removeEdge(edge);
                }
            }
        
        // Rajoute des obstacles horizontaux
        for (int i = 1210; i < 1235; i++)
        {
            vcolor.put("v" +i, Color.gray);
            // Delete all edges to these points
            Collection<Number> edges = new ArrayList<>();
            edges = g.getIncidentEdges("v"+i);
            for (Number edge : edges)
            {
                g.removeEdge(edge);
            }
        }
        
        // Rajoute des obstacles verticaux
        for (int i = 0; i < 30; i++)
        {
            int j = 1235 - i*40;
            vcolor.put("v" +j, Color.gray);
            // Delete all edges to these points
            Collection<Number> edges = new ArrayList<>();
            edges = g.getIncidentEdges("v"+j);
            for (Number edge : edges)
            {
                g.removeEdge(edge);
            }
        }
    }

    private Graph<String,Number> generateVertexGrid(Map<String,Point2D> vlf, Dimension d, int interval) {
        int count = d.width/interval * d.height/interval;
        Graph<String,Number> graph = new SparseGraph<String,Number>();
        int edgeCount = 0;
        for(int i=0; i<count; i++) {
            int x = interval*i;
            int y = x / d.width * interval;
            x %= d.width;
            
            Point2D location = new Point2D.Float(x, y);
            String vertex = "v"+i;
            vlf.put(vertex, location);
            graph.addVertex(vertex);
            
            if (i%(d.width/interval) != 0)
            {
                graph.addEdge(edgeCount, "v"+(i-1), "v"+i);
                edgeCount ++;
            }
            if (i < count - (d.height/interval))
            {
                graph.addEdge(edgeCount, "v"+i, "v"+(i+d.width/interval));
                edgeCount ++;
            }            
        }
        return graph;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        pack();
    }// </editor-fold>                        
}
