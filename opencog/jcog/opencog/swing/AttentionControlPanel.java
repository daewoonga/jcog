/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jcog.opencog.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import jcog.opencog.Atom;
import jcog.opencog.MindAgent;
import jcog.opencog.OCMind;
import jcog.spacegraph.swing.SwingWindow;
import org.apache.commons.collections15.IteratorUtils;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 * Panel for manually invoking mindagents
 * @author seh
 */
public class AttentionControlPanel extends JPanel {

    int maxAtoms = 64;
    //final short boostAmount = 50;
    private final OCMind mind;
    private HistogramDataset stiDataset;
    //private JFreeChart stiHistogram;
    //private ChartPanel stiChart;
    private JPanel stiChart;
    
//    Map<MindAgent, MindAgentPanel> mindAgentPanels = new WeakHashMap();
    private final ControlPanelAgent agent;
    private final List<AtomPanel> atomPanels = new LinkedList();

    class ControlPanelAgent extends MindAgent {

        public ControlPanelAgent(double period) {
            super();
            setPeriod(period);
        }

        @Override
        protected void run(OCMind mind) {
            for (Atom a : pinned) {
                addStimulus(a, (short) (Short.MAX_VALUE / 2));
            }
            refresh();
        }
    }

    /** panel representing a mind agent:
     *   --checkbox indicating whether it will be included in a cycle
     *   --button for immediate invocation
     *   --indicator for last execution time (changes color of background)
     */
//    public class MindAgentPanel extends JPanel {
//
//        public final JCheckBox cycleEnabled;
//        public final MindAgent agent;
//
//        public MindAgentPanel(final MindAgent m) {
//            super(new FlowLayout());
//
//            this.agent = m;
//
//            cycleEnabled = new JCheckBox();
//            cycleEnabled.setSelected(true);
//            add(cycleEnabled);
//
//            JMenu nameMenu = new JMenu(m.toString());
//            {
//                JMenuItem runNowItem = new JMenuItem("Run Now");
//                runNowItem.addActionListener(new ActionListener() {
//
//                    @Override
//                    public void actionPerformed(ActionEvent ae) {
//                        runAgent(MindAgentPanel.this);
//                    }
//                });
//                
//                nameMenu.add(runNowItem);
//                
//                nameMenu.addSeparator();
//                
//                for (final Method a : m.getClass().getMethods()) {
//                    if (Modifier.isPublic(a.getModifiers())) {
//                        if (a.getParameterTypes().length == 0) {
//                            JMenuItem j = new JMenuItem(a.getName());
//                            j.addActionListener(new ActionListener() {
//                                @Override
//                                public void actionPerformed(ActionEvent e) {
//                                    try {
//                                        a.invoke(m);
//                                    } catch (IllegalAccessException ex) {
//                                        Logger.getLogger(AttentionControlPanel.class.getName()).log(Level.SEVERE, null, ex);
//                                    } catch (IllegalArgumentException ex) {
//                                        Logger.getLogger(AttentionControlPanel.class.getName()).log(Level.SEVERE, null, ex);
//                                    } catch (InvocationTargetException ex) {
//                                        Logger.getLogger(AttentionControlPanel.class.getName()).log(Level.SEVERE, null, ex);
//                                    }
//                                }                                
//                            });
//                            nameMenu.add(j);
//                        }
//                    }
//                }
//            }
//            
//            JMenuBar jb = new JMenuBar();
//            jb.add(nameMenu);
//            add(jb);
//
//        }
//    }
    public AttentionControlPanel(OCMind mind, double updatePeriod) {
        super(new BorderLayout());

        this.mind = mind;

        this.agent = new ControlPanelAgent(updatePeriod);
        mind.addAgent(agent);

//        cycle = new JButton("Update");
//        cycle.setMnemonic('u');
//        cycle.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                SwingUtilities.invokeLater(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        refresh();
//                    }
//                });
//            }
//        });

        //refresh();
    }
//    protected MindAgentPanel getMindAgentPanel(MindAgent m) {
//        MindAgentPanel map = mindAgentPanels.get(m);
//        if (map == null) {
//            map = new MindAgentPanel(m);
//            mindAgentPanels.put(m, map);
//        }
//        return map;
//    }
    public final Map<Atom, JButton> atomButtons = new WeakHashMap();
    public final Set<Atom> pinned = new HashSet();

//    public void setPin(final Atom a, boolean enabled) {
//        if (enabled) {            
//            pinned.add(a);
//        }
//        else {
//            pinned.remove(a);
//        }
//    }
    public static String minString(String x, int maxChars) {
        if (x.length() < maxChars) {
            return x;
        }
        return x.substring(0, maxChars - 2) + "..";
    }

    public JButton getButton(final Atom a) {
        JButton j = atomButtons.get(a);

        String name = mind.getName(a);
        if (name == null) {
            name = a.uuid.toString();
        }
        final String updatedName = mind.getTypeName(a) + " " + (mind.getSTI(a)) + " " + minString(name, 32);

        if (j == null) {
            j = new JButton(updatedName);

            final JButton fj = j;
            j.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    newAtomView(a);
                }
            });
            atomButtons.put(a, j);
        } else {
            j.setText(updatedName);    
            j.updateUI();
        }
        return j;
    }

    public void refresh() {

        int bins = 25;

        if (!isDisplayable()) {
            return;
        }
        if (!isVisible()) {
            return;
        }

        removeAll();

        //TODO optimize this by streaming
        List<Atom> _atoms = IteratorUtils.toList(mind.iterateAtomsByDecreasingSTI());
        
        if (_atoms.size() > 0) {
            double[] stis = new double[_atoms.size()];
            //double[] ltis = new double[atoms.size()];
            int i = 0;
            for (Atom a : _atoms) {
                stis[i] = mind.getSTI(a);
                //ltis[i] = mind.getLTI(a);
                i++;
            }
            stiDataset = new HistogramDataset();

            stiDataset.setType(HistogramType.FREQUENCY);
            stiDataset.addSeries("STI", stis, bins);
            //stiDataset.addSeries("LTI", ltis, 10);
            
        }

        final List<Atom> atoms = (_atoms.size() > maxAtoms) ? _atoms.subList(0, maxAtoms) : _atoms;

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JPanel atomPanel = new JPanel();
                atomPanel.setLayout(new BoxLayout(atomPanel, BoxLayout.PAGE_AXIS));
                {


                    for (final Atom a : atoms) {
                        try {
                            final String type = mind.getTypeName(a);
                            final double normSTI = mind.getNormalizedSTI(a);

                            JButton b = getButton(a);

                            float hue = ((float) (type.hashCode() % 19)) / 19.0f;
                            float bri = (float) normSTI * 0.25f + 0.75f;

                            b.setBackground(new Color(Color.HSBtoRGB(hue, 0.8f, bri)));

                            b.setAlignmentX(Component.LEFT_ALIGNMENT);
                            atomPanel.add(b);
                        } catch (NullPointerException npe) {
                            System.out.println("missing atom information: " + a);
                        }
                    }
                }

                JPanel agentPanel = new JPanel(new BorderLayout());
                {
                    //            JPanel agentList = new JPanel();
                    //            agentList.setLayout(new BoxLayout(agentList, BoxLayout.PAGE_AXIS));
                    //            {
                    //                for (MindAgent m : mind.getAgents()) {
                    //                    agentList.add(new JLabel(m) /* getMindAgentPanel(m) */ );
                    //                }
                    //                agentList.add(Box.createVerticalBox());
                    //
                    //            }
                    //            agentPanel.add(new JScrollPane(agentList), BorderLayout.CENTER);
                    //
                    //            agentPanel.add(cycle, BorderLayout.SOUTH);
                }

                JPanel statsPanel = new JPanel();
                statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.PAGE_AXIS));
                {

                    if (atoms.size() > 0) {
//                        stiHistogram = ChartFactory.createHistogram("STI Distribution", "Importance", "Count", stiDataset, PlotOrientation.VERTICAL, false, false, false);
//
//                        stiChart = new ChartPanel(stiHistogram, true, true, true, true, true);

                        stiChart = new JPanel() {
                             public void paint(Graphics g) {
                                // Dynamically calculate size information
                                Dimension size = getSize();

                                g.setColor(Color.BLACK);
                                g.fillRect(0,0,(int)size.getWidth(),(int)size.getHeight());
                                
//                                // diameter
//                                int d = Math.min(size.width, size.height); 
//                                int x = (size.width - d)/2;
//                                int y = (size.height - d)/2;
//
//                                // draw circle (color already set to foreground)
//                                g.fillOval(x, y, d, d);
//                                g.setColor(Color.black);
//                                g.drawOval(x, y, d, d);
                                
                                double minX=0, minY=0, maxX=0, maxY=0;
                                
                                int numBins = stiDataset.getItemCount(0);
                                for (int i = 0; i < numBins; i++) {
                                    if (i == 0) {
                                        minX = stiDataset.getStartXValue(0, i);
                                        maxX = stiDataset.getEndXValue(0, i);
                                        minY = stiDataset.getStartYValue(0, i);
                                        maxY = stiDataset.getEndYValue(0, i);
                                    }
                                    else {
                                        double lsX = stiDataset.getStartXValue(0, i);
                                        double leX = stiDataset.getEndXValue(0, i);
                                        double lsY = stiDataset.getStartYValue(0, i);
                                        double leY = stiDataset.getEndYValue(0, i);
                                        
                                        if (lsX < minX) minX = lsX;
                                        if (lsY < minY) minY = lsY;
                                        if (leX > maxX) maxX = leX;
                                        if (leY > maxY) maxY = leY;
                                    }
                                    
                                }
                                double width = maxX - minX;
                                double height = maxY - minY;
                                for (int i = 0; i < stiDataset.getItemCount(0); i++) {
                                            
                                    double p = (stiDataset.getEndY(0, i).doubleValue() / maxY);
                                    
                                    float hue = (float)(0.0 + (1.0 - p) * 0.25);
                                    float bri = (float)(0.75 + 0.25 * p);
                                    g.setColor(Color.getHSBColor(hue, 0.6f, bri));
                                    
                                    double w = size.getWidth() / numBins;
                                    
                                    double hh = size.getHeight() * p;
                                    g.fillRect((int)(w*i), (int)(size.getHeight() - hh), (int)w, (int)hh);
                                }
                            }
                        };
                        
                        statsPanel.add(stiChart);
                        
                        
                    } else {
                    }
                }

                //add(agentPanel, BorderLayout.WEST);
                add(new JScrollPane(atomPanel), BorderLayout.WEST);
                add(new JScrollPane(statsPanel), BorderLayout.CENTER);

                updateUI();
            }
        });

        for (AtomPanel ap : atomPanels) {
            ap.refresh();
        }
    }

//    protected void runAgent(MindAgentPanel m) {
//        //TODO calculate runtime of each agent to display
//        m.agent._run(mind, 1.0);
//    }
//    protected void cycle() {        
////        for (MindAgent m : mind.getAgents()) {
////            MindAgentPanel map = getMindAgentPanel(m);
////            if (map.cycleEnabled.isSelected()) {
////                runAgent(map);
////            }
////        }
//
//        refresh();
//    }
    public JFrame newWindow() {
        final JFrame jf = new JFrame(getClass().getSimpleName());
        jf.getContentPane().add(this);
        jf.setSize(1200, 600);
        jf.setVisible(true);

        return jf;
    }

    public void newAtomView(final Atom a) {
        final AtomPanel ap = new AtomPanel(mind, a);
        JFrame jf = new SwingWindow(ap, 400, 300);
        jf.setTitle(mind.getName(a));
        jf.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                atomPanels.add(ap);
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                atomPanels.remove(ap);
            }
//            @Override
//            public void windowIconified(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public void windowDeiconified(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public void windowActivated(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public void windowDeactivated(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
        });
    }
}
