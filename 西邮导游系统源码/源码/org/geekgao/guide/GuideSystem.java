package org.geekgao.guide;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class GuideSystem extends JFrame{

	//软件左上角图标
	private Image icon = GuideUtil.getImage("images/icon.png");
	final private JButton firstPanelDownBut1 = new JButton("最短路径");
	final private JButton firstPanelDownBut2 = new JButton("擦除线路");
	final private JButton rightBut1 = new JButton("路经查询");
	final private JButton rightBut2 = new JButton("景点信息");
	final private JButton rightBut3 = new JButton("添加节点");
	final private JButton rightBut4 = new JButton("删除节点");
	final private JButton rightBut5 = new JButton("添加景点");
	final private JButton rightBut6 = new JButton("删除景点");
	final private JButton rightBut7 = new JButton("图的算法");
	private JPanel cardPanel;//容纳很多panel的卡片panel
	private CardLayout card;//卡片布局管理器
	JComboBox<String> firstPanelDownBox1;
	JComboBox<String> firstPanelDownBox2;
	
	/**
	 * 这里面存储着一条路径上面的所有点，挨个在他们之间画线表示路径
	 * 存储一个路径上的节点的序号
	 */
	private Integer[] paintVertex;
	
	/**
	 * 下面这两个是图片的坐标原点
	 * windows7下是（3,25）
	 * Linux下面好像没有，这个有平台相关
	 */
	private static final int borderWidth = 3;
	private static final int borderHeight = 25;
	
	private Map<Integer,Vertex> map;//存图
	private Map<String,String> view;//景点的名称与后面的景点的点的序号和简介对应
	private Map<Integer,String> viewNumNameMap;//景点序号和名称对应
	private JLabel secondPanelLab;
	private JList<String> secondPanelList;
	private JLabel thirdPanelUpMapLabel;
	private JLabel thirdPanelDownLab2;
	private JTextField thirdPanelDownText1;
	private JButton thirdPanelDownButton1;
	private JPanel thirdPanelUp;
	private JTextField fourthPanelDownText;
	private JButton fourthPanelDownButton;//确认删除节点按钮
	
	private JButton clickedButton;//标记被按下的是哪个按钮
	private JTextField fifthPanelDownText1;
	private JTextField fifthPanelDownText2;
	private JButton fifthPanelDownButton;
	private JTextField fifthPanelDownText3;
	private JTextField sixthPanelDownText;
	private JButton sixthPanelDownButton;
	
	private static final String vertexPath = "C:/Users/geekgao/Desktop/课程设计相关/data.txt";
	private static final String viewPath = "C:/Users/geekgao/Desktop/课程设计相关/view.txt";
	private JButton sevenPanelBfsButton;
	private JButton sevenPanelDfsButton;
	
	public static void main(String[] args) {
		
		GuideSystem guide = new GuideSystem();
		guide.paintGuideWindow();
		
	}
	
	public GuideSystem() {
		
		map = GuideUtil.getVertex(vertexPath);//读入节点信息
		view = GuideUtil.getView(viewPath);//读入景点信息
		
		Set<String> viewNameSet = view.keySet();
		viewNumNameMap = new HashMap<Integer, String>();
		for (String name:viewNameSet) {
			viewNumNameMap.put(Integer.valueOf(view.get(name).split(" ")[0]),name);
		}
		
	}
	
	@Override
	/**
	 * 画出路径
	 */
public void paint(Graphics g) {
		
		super.paint(g);
		Color c = g.getColor();
		g.setColor(Color.RED);
		float lineWidth = 4.0f;//线条宽度
	    ((Graphics2D)g).setStroke(new BasicStroke(lineWidth));
		
	    //按下的是路经查询
	    if (clickedButton == rightBut1 ||
	    	clickedButton == firstPanelDownBut1) {
	    	if (paintVertex == null) {
				return;
			}
			
		    for (int i = 0;i + 1 < paintVertex.length;i++) {
		    	g.drawLine(	map.get(paintVertex[i]).x + borderWidth, 
		    				map.get(paintVertex[i]).y + borderHeight, 
		    				map.get(paintVertex[i+1]).x + borderWidth, 
		    				map.get(paintVertex[i+1]).y + borderHeight);
		    }
		    g.setFont(new Font("微软雅黑", Font.BOLD, 15));
		    g.setColor(Color.YELLOW);
		    g.drawString("起点", 	map.get(paintVertex[0]).x + borderWidth, 
		    					map.get(paintVertex[0]).y + borderHeight);
		    g.drawString("终点", 	map.get(paintVertex[paintVertex.length - 1]).x + borderWidth, 
		    					map.get(paintVertex[paintVertex.length - 1]).y + borderHeight);
	    } else if (	clickedButton == rightBut3 || 
	    			clickedButton == rightBut4 || 
	    			clickedButton == rightBut5 ||
	    			clickedButton == rightBut6 ||
	    			clickedButton == fourthPanelDownButton || 
	    			clickedButton == thirdPanelDownButton1 ||
	    			clickedButton == fifthPanelDownButton  ||
	    			clickedButton == sixthPanelDownButton) {
	    	Set<Integer> vexNum = map.keySet();
	    	Set<String> viewName = view.keySet();
	    	Set<Integer> viewNum = new HashSet<Integer>();//存储景点的编号，景点的点颜色要区分出来
	    	
	    	for (String s:viewName) {
	    		String introduce = view.get(s);
	    		viewNum.add(Integer.valueOf(introduce.split(" ")[0]));
	    	}
	    	
	    	Vertex t;
	    	int r = 3;//点圆的半径
	    	//遍历图的节点，在每个节点上画一个点
	    	for (Integer i:vexNum) {
	    		t = map.get(i);
	    		if (viewNum.contains(i)) {
	    			g.setColor(Color.YELLOW);
	    		} else {
	    			g.setColor(Color.RED);
	    		}
	    		g.fillOval(t.x + borderWidth - r, t.y + borderHeight - r, 2 * r, 2 * r);
	    	}
	    	g.setFont(new Font("微软雅黑", Font.BOLD, 12));
		    g.setColor(Color.BLACK);
		    
		    //点的上面打印点的序号
		    for (Integer i:vexNum) {
	    		t = map.get(i);
	    		g.drawString(String.valueOf(i), t.x + borderWidth, t.y + borderHeight);
	    	}
	    }
	    
		g.setColor(c);
		
	}
	
	public void paintGuideWindow() {
		
		setTitle("西邮导游系统 - geekgao");
		setIconImage(icon);
		/**
		 * 窗口关闭
		 */
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
			
		});
		
//		===============================================================
		/**
		 * 设计窗口
		 */
		Container c = getContentPane();//获取窗口内容面板
		card = new CardLayout();
		cardPanel = new JPanel(card);//左侧的卡片panel
		JPanel controlPanel = new JPanel(new GridLayout(10,1,0,5));//右侧的控制卡片panel，中间放按钮
		c.add(cardPanel,BorderLayout.WEST);
		c.add(controlPanel,BorderLayout.EAST);
		
		/**
		 * 控制按钮
		 */
		controlPanel.add(rightBut1);
		controlPanel.add(rightBut2);
		controlPanel.add(rightBut3);
		controlPanel.add(rightBut4);
		controlPanel.add(rightBut5);
		controlPanel.add(rightBut6);
		controlPanel.add(rightBut7);
		rightBut1.addActionListener(new MyButtonActionListener());
		rightBut2.addActionListener(new MyButtonActionListener());
		rightBut3.addActionListener(new MyButtonActionListener());
		rightBut4.addActionListener(new MyButtonActionListener());
		rightBut5.addActionListener(new MyButtonActionListener());
		rightBut6.addActionListener(new MyButtonActionListener());
		rightBut7.addActionListener(new MyButtonActionListener());
		
		/**
		 * 左侧东西每个都是panel，再给这个panel里面添加东西
		 */
		
		/**
		 * 第一个卡片页
		 */
		JPanel firstPanel = new JPanel(new BorderLayout());
		cardPanel.add(firstPanel);//当前panel加到cardPanel里面
		
		JPanel firstPanelUp = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		firstPanel.add(firstPanelUp,BorderLayout.NORTH);
		JLabel firstPanelMapLabel = new JLabel(new ImageIcon("src/images/map.jpg"));
		firstPanelUp.add(firstPanelMapLabel);
		
		/**
		 * 下面这个就是选择地点那一坨
		 */
		JPanel firstPanelDown = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
		firstPanel.add(firstPanelDown,BorderLayout.SOUTH);
		JLabel firstPanelDownlab1 = new JLabel("起点");
		firstPanelDown.add(firstPanelDownlab1);
		firstPanelDownBox1 = new JComboBox<String>();
		firstPanelDown.add(firstPanelDownBox1);
		JLabel firstPanelDownlab2 = new JLabel("终点");
		firstPanelDown.add(firstPanelDownlab2);
		firstPanelDownBox2 = new JComboBox<String>();
		firstPanelDown.add(firstPanelDownBox2);
		setViewBox();
		
		firstPanelDown.add(firstPanelDownBut1);
		firstPanelDown.add(firstPanelDownBut2);
		firstPanelDownBut1.addActionListener(new MyButtonActionListener());
		firstPanelDownBut2.addActionListener(new MyButtonActionListener());
		
		/**
		 * 第二个卡片页
		 */
		JPanel secondPanel = new JPanel(new BorderLayout());
		cardPanel.add(secondPanel);

		secondPanelList = new JList<String>();
		secondPanelList.setModel(new DefaultListModel<String>());
		JScrollPane scrollPane = new JScrollPane(secondPanelList);//滚动的列表
		secondPanel.add(scrollPane,BorderLayout.WEST);
		
		secondPanelLab = new JLabel();
		secondPanelLab.setHorizontalAlignment(JLabel.CENTER);
		secondPanel.add(secondPanelLab,BorderLayout.CENTER);
		
		setViewNameList();
		
		//给List添加鼠标单击事件
		secondPanelList.addMouseListener(new MyMouseListener());
		
		/**
		 * 第三个卡片页
		 */
		JPanel thirdPanel = new JPanel(new BorderLayout());
		cardPanel.add(thirdPanel);
		
		thirdPanelUp = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		thirdPanel.add(thirdPanelUp);
		thirdPanelUpMapLabel = new JLabel(new ImageIcon("src/images/map.jpg"));
		thirdPanelUp.add(thirdPanelUpMapLabel,FlowLayout.LEFT);
		thirdPanelUpMapLabel.addMouseListener(new MyMouseListener());//给存放图片Label添加鼠标事件监听
		
		JPanel thirdPanelDown = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
		thirdPanel.add(thirdPanelDown,BorderLayout.SOUTH);
		JLabel thirdPanelDownLab1 = new JLabel("点图选择点:");
		thirdPanelDown.add(thirdPanelDownLab1);
		thirdPanelDownLab2 = new JLabel();
		thirdPanelDown.add(thirdPanelDownLab2);
		JLabel thirdPanelDownLab3= new JLabel("和哪些点有联系(逗号隔开):");
		thirdPanelDown.add(thirdPanelDownLab3);
		thirdPanelDownText1 = new JTextField(14);
		thirdPanelDown.add(thirdPanelDownText1);
		thirdPanelDownButton1 = new JButton("确定添加");
		thirdPanelDown.add(thirdPanelDownButton1);
		thirdPanelDownButton1.addActionListener(new MyButtonActionListener());
		
		/**
		 * 第四个卡片页
		 */
		JPanel fourthPanel = new JPanel(new BorderLayout());
		cardPanel.add(fourthPanel);
		
		JPanel fourthPanelUp = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		fourthPanel.add(fourthPanelUp,BorderLayout.NORTH);
		JLabel fourthPanelUpMapLabel = new JLabel(new ImageIcon("src/images/map.jpg"));
		fourthPanelUp.add(fourthPanelUpMapLabel,FlowLayout.LEFT);
		JPanel fourthPanelDown = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
		fourthPanel.add(fourthPanelDown,BorderLayout.SOUTH);
		JLabel fourthPanelDownLabel = new JLabel("要删除的节点序号(逗号隔开):");
		fourthPanelDown.add(fourthPanelDownLabel);
		fourthPanelDownText = new JTextField(15);
		fourthPanelDown.add(fourthPanelDownText);
		fourthPanelDownButton = new JButton("确定删除");
		fourthPanelDown.add(fourthPanelDownButton);
		fourthPanelDownButton.addActionListener(new MyButtonActionListener());
		
		/**
		 * 第五个卡片页
		 */
		JPanel fifthPanel = new JPanel(new BorderLayout());
		cardPanel.add(fifthPanel);
		
		JPanel fifthPanelUp = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		fifthPanel.add(fifthPanelUp,BorderLayout.NORTH);
		JLabel fifthPanelUpMapLabel = new JLabel(new ImageIcon("src/images/map.jpg"));
		fifthPanelUp.add(fifthPanelUpMapLabel,FlowLayout.LEFT);
		JPanel fifthPanelDown = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
		fifthPanel.add(fifthPanelDown,BorderLayout.SOUTH);
		JLabel fifthPanelDownLabel1 = new JLabel("输入节点序号:");
		fifthPanelDown.add(fifthPanelDownLabel1);
		fifthPanelDownText1 = new JTextField(2);
		fifthPanelDown.add(fifthPanelDownText1);
		JLabel fifthPanelDownLabel2 = new JLabel("景点命名:");
		fifthPanelDown.add(fifthPanelDownLabel2);
		fifthPanelDownText2 = new JTextField(7);
		fifthPanelDown.add(fifthPanelDownText2);
		JLabel fifthPanelDownLabel3 = new JLabel("景点介绍:");
		fifthPanelDown.add(fifthPanelDownLabel3);
		fifthPanelDownText3 = new JTextField(10);
		fifthPanelDown.add(fifthPanelDownText3);
		fifthPanelDownButton = new JButton("确定添加");
		fifthPanelDown.add(fifthPanelDownButton);
		fifthPanelDownButton.addActionListener(new MyButtonActionListener());
		
		/**
		 * 第六个卡片页
		 */
		JPanel sixthPanel = new JPanel(new BorderLayout());
		cardPanel.add(sixthPanel);
		
		JPanel sixthPanelUp = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		sixthPanel.add(sixthPanelUp,BorderLayout.NORTH);
		JLabel sixthPanelUpMapLabel = new JLabel(new ImageIcon("src/images/map.jpg"));
		sixthPanelUp.add(sixthPanelUpMapLabel,FlowLayout.LEFT);
		JPanel sixthPanelDown = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
		sixthPanel.add(sixthPanelDown,BorderLayout.SOUTH);
		JLabel sixthPanelDownLabel = new JLabel("要删除的景点序号(黄色点代表景点)(逗号隔开):");
		sixthPanelDown.add(sixthPanelDownLabel);
		sixthPanelDownText = new JTextField(15);
		sixthPanelDown.add(sixthPanelDownText);
		sixthPanelDownButton = new JButton("确定删除");
		sixthPanelDown.add(sixthPanelDownButton);
		sixthPanelDownButton.addActionListener(new MyButtonActionListener());
		
		/**
		 * 第七个卡片页
		 * 算法页
		 */
		JPanel seventhPanel = new JPanel(new FlowLayout());
		cardPanel.add(seventhPanel);
		
		sevenPanelBfsButton = new JButton("BFS算法");
		sevenPanelDfsButton = new JButton("DFS算法");
		
		seventhPanel.add(sevenPanelDfsButton);
		seventhPanel.add(sevenPanelBfsButton);
		
		MyButtonActionListener buttonListener = new MyButtonActionListener();
		sevenPanelBfsButton.addActionListener(buttonListener);
		sevenPanelDfsButton.addActionListener(buttonListener);
		
		
//		===============================================================
		
		setResizable(false);//不可更改大小
		setVisible(true);
		pack();
		setLocationRelativeTo(null);//这句话放在pack()后面，否则开始窗口大小为0，左上角在屏幕中点
		
	}
	
	public int getStartNum() {
		String viewNameStart = (String) firstPanelDownBox1.getSelectedItem();
		String viewLastStart = view.get(viewNameStart);
		return Integer.parseInt((viewLastStart.split(" "))[0]);
	}
	
	public int getEndNum() {
		String viewNameEnd = (String) firstPanelDownBox2.getSelectedItem();
		String viewLastEnd = view.get(viewNameEnd);
		return Integer.parseInt((viewLastEnd.split(" "))[0]);
	}
	
	public int getMaxNum() {
		/**
		 * 获得最大的节点编号
		 */
		Set<Integer> vexNum = map.keySet();
		int max = 0;//序号从1开始
		for (Integer i:vexNum) {
			if (max < i) {
				max = i;
			}
		}
		
		return max;
	}
	
	public void setViewBox() {
		
		firstPanelDownBox1.removeAllItems();
		firstPanelDownBox2.removeAllItems();
		
		Set<String> viewName = view.keySet();
		for (String s:viewName) {
			firstPanelDownBox1.addItem(s);
			firstPanelDownBox2.addItem(s);
		}
	}
	
	private void setViewNameList() {
		
		DefaultListModel<String> model = (DefaultListModel<String>) secondPanelList.getModel();
		model.clear();
		
		Set<String> viewNameSet = view.keySet();
		for (String viewName:viewNameSet) {
			model.addElement(viewName);
		}
		secondPanelLab.setText("");//可能就是删除了原先查看的那个内容，所以需要删除Label中的内容
	}
	
	class MyButtonActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			clickedButton = (JButton) e.getSource();
			if (clickedButton == firstPanelDownBut1) {
				/**
				 * 最短路径的按钮
				 */
				int numStart = getStartNum();
				int numEnd = getEndNum();
				int count = getMaxNum();//因为数组下标和路径序号相对应，所以建立的数组的大小应该包括最大的序号
				paintVertex = GuideAlgorithm.Dijkstra(numStart, numEnd, count,map);
				
				if (paintVertex == null) {
					JOptionPane.showMessageDialog(null, "没有路径可以到达或者起点与终点相同！请确定所有节点都连通。");
				}
				
				repaint();
			} else if (clickedButton == firstPanelDownBut2) {
				/**
				 * 擦出路线按钮
				 */
				paintVertex = null;
				repaint();
			} else if (clickedButton == thirdPanelDownButton1) {
				/**
				 * 确认添加节点的按钮
				 */
				
				String xy = thirdPanelDownLab2.getText();
				String relationNum = thirdPanelDownText1.getText();
				if (xy.equals("") || relationNum.equals("")) {
					JOptionPane.showMessageDialog(null, "有数据未输入!");
					return;
				}
				
				Set<Integer> relationNumSet = new HashSet<Integer>();
				String[] relationNumArray = relationNum.split(",");
				for (int i = 0;i < relationNumArray.length;i++) {
					try {
						relationNumSet.add(Integer.valueOf(relationNumArray[i]));
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(null, "输入不正确，只能输入数字!");
						return;
					}
				}
				
				//如果用户写的点不存在，那么不能使之添加成功
				for (Integer i:relationNumSet) {
					if (!map.containsKey(i)) {
						JOptionPane.showMessageDialog(null, "您输入了地图上不存在的点!");
						return;
					}
				}
				thirdPanelDownText1.setText("");
				
				int vexNum;//即将添加到图中的节点序号
				for (vexNum = 1;map.containsKey(vexNum);vexNum++);//选择一个最小的并且未加入图的节点序号
				
				Vertex newVex = new Vertex();
				newVex.num = vexNum;
				newVex.x = Integer.parseInt(xy.split(",")[0]);
				newVex.y = Integer.parseInt(xy.split(",")[1]);
				newVex.pointNum = new HashMap<Integer, Integer>();
				
				//构建新加入的点的信息
				for (Integer i:relationNumSet) {
					int x = map.get(i).x;
					int y = map.get(i).y;
					int distance = (int) Math.sqrt((x-newVex.x) * (x-newVex.x) + (y-newVex.y) * (y-newVex.y));
					newVex.pointNum.put(i, distance);
				}
				map.put(newVex.num, newVex);
				
				//修改与新加的点有关系的点的信息，那些点也与新加的点有关系
				for (Integer i:relationNumSet) {
					Vertex t = map.get(i);
					t.pointNum.put(newVex.num, newVex.pointNum.get(i));
					map.put(i, t);
				}
				
				GuideUtil.setVertex(map, vertexPath);
				repaint();
			} else if (clickedButton == fourthPanelDownButton) {
				/**
				 * 确认删除节点按钮
				 */
				
				if (fourthPanelDownText.getText().equals("")) {
					return;
				}
				
				String[] deleteNumStr = fourthPanelDownText.getText().split(",");
				Set<Integer> deleteNum = new HashSet<Integer>();
				for (String s:deleteNumStr) {
					try{
						deleteNum.add(Integer.valueOf(s));
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(null, "输入不正确，只能输入数字和逗号!");
						return;
					}
				}
				fourthPanelDownText.setText("");
				
//				删除这个点的信息,遍历即将被删除的点的序号的集合
				for (Integer i:deleteNum) {
					//图中删除这个点
					if (map.containsKey(i)) {
						map.remove(i);
					}
				}
				
				Set<Integer> vexNum = map.keySet();//图中的点的序号
				Map<Integer,Integer> pointNum;//某个节点指向什么点
				for (Integer i:vexNum) {
					pointNum = map.get(i).pointNum;
					for (Integer j:deleteNum) {
						if (pointNum.containsKey(j)) {
							pointNum.remove(j);
						}
					}
				}
				GuideUtil.setVertex(map, vertexPath);
				
				//这个节点可能是一个景点，可能有介绍信息，也要删除
				Set<String> viewName = view.keySet();
				String t;
				for (Iterator<String> it = viewName.iterator();it.hasNext();) {
					t = view.get(it.next()).split(" ")[0];//得到这个景点的序号
					if (deleteNum.contains(Integer.valueOf(t))) {
						it.remove();
					}
				}
				GuideUtil.setView(view, viewPath);
				
				setViewBox();//重新显示景点BOX
				setViewNameList();//重新显示选择地点的那两个下拉框
				
				paintVertex = null;//删除节点后就不显示路径了，即时原来选择的路径的起点和终点还在
				
				repaint();
			} else if (clickedButton == fifthPanelDownButton) {
				/**
				 * 确定添加景点按钮
				 */
				
				String viewNum = fifthPanelDownText1.getText();
				String viewName = fifthPanelDownText2.getText();
				String viewIntroduce = fifthPanelDownText3.getText();
				if (viewNum.equals("") || viewName.equals("") || viewIntroduce.equals("")) {
					JOptionPane.showMessageDialog(null, "有数据未输入!");
					return;
				}
				
				//点不存在不加入
				try {
					if (!map.containsKey(Integer.valueOf(viewNum))) {
						JOptionPane.showMessageDialog(null, "节点不存在!");
						return;
					}
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(null, "节点序号输入不正确，只能输入数字!");
					return;
				}
				
				Set<String> viewNameSet = view.keySet();
				for (String name:viewNameSet) {
					if (view.get(name).split(" ")[0].equals(viewNum)) {
						JOptionPane.showMessageDialog(null, "此节点已是景点，要更改请删除后再添加!");
						return;
					}
				}
				
				fifthPanelDownText1.setText("");
				fifthPanelDownText2.setText("");
				fifthPanelDownText3.setText("");
				
				view.put(viewName, viewNum + " " +viewIntroduce);
				
				GuideUtil.setView(view, viewPath);
				
				setViewBox();//重新显示景点BOX
				setViewNameList();//重新显示选择地点的那两个下拉框
				
				repaint();
			} else if (clickedButton == sixthPanelDownButton) {
				/**
				 * 确定删除景点按钮
				 */
				
				String inputText = sixthPanelDownText.getText();
				if (inputText.equals("")) {
					JOptionPane.showMessageDialog(null, "数据未输入!");
					return;
				}
				
				Set<Integer> deleteNumSet = new HashSet<Integer>();
				String[] deleteNumArray = inputText.split(",");
				for (int i = 0;i < deleteNumArray.length;i++) {
					try {
						deleteNumSet.add(Integer.valueOf(deleteNumArray[i]));
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(null, "只能输入数字和逗号!");
						return;
					}
				}
				
				Set<Integer> viewNumSet = new HashSet<Integer>();//存储景点的编号,判断是否要删除的点是否是一个景点
				Set<String> viewNameSet = view.keySet();
				for (String viewName:viewNameSet) {
					viewNumSet.add(Integer.valueOf(view.get(viewName).split(" ")[0]));
				}
				for (Integer i:deleteNumSet) {
					if (!viewNumSet.contains(i)) {
						JOptionPane.showMessageDialog(null, "您输入的点中包括不是景点的点!");
						return;
					}
				}
				sixthPanelDownText.setText("");
				
				for (Iterator<String> it = viewNameSet.iterator();it.hasNext();) {
					if (deleteNumSet.contains(Integer.valueOf(view.get(it.next()).split(" ")[0]))) {
						it.remove();
					}
				}
				
				setViewBox();
				setViewNameList();
				GuideUtil.setView(view, viewPath);
				
				repaint();
			} else if (	clickedButton == sevenPanelBfsButton ||
						clickedButton == sevenPanelDfsButton) {
				/**
				 * 算法那一坨
				 */
				
				JFrame newWindow = new JFrame();
				Container c = newWindow.getContentPane();

				JLabel roadText = new JLabel();
				JScrollPane scrollPanel = new JScrollPane(roadText);
				c.add(scrollPanel);
				
				int startNum = 1;//从最小的点开始深度遍历
				int i;
				for (i = 1;i <= getMaxNum() && !map.containsKey(i);i++);
				startNum = i;
				Integer[] roadVexNum = null;
				if (clickedButton == sevenPanelBfsButton) {
					roadVexNum = GuideAlgorithm.Bfs(startNum, map, view,viewNumNameMap);
					newWindow.setTitle("广度优先遍历");
				} else if (clickedButton == sevenPanelDfsButton) {
					roadVexNum = GuideAlgorithm.Dfs(startNum, map, view,viewNumNameMap);
					newWindow.setTitle("深度优先遍历");
				}
				
				StringBuffer roadStr = new StringBuffer();
				roadStr.append("<html>");
				roadStr.append(viewNumNameMap.get(roadVexNum[0]));
				int length = 0;
				for (i = 1;i < roadVexNum.length;i++) {
					roadStr.append("==>" + viewNumNameMap.get(roadVexNum[i]));
					length += viewNumNameMap.get(roadVexNum[i]).length();
					
					if (length > 30) {
						length = 0;
						roadStr.append("<br>");
					}
				}
				roadStr.append("</html>");
				roadText.setText(roadStr.toString());
				
				newWindow.pack();
				newWindow.setLocationRelativeTo(null);//这句话放在pack()后面，否则开始窗口大小为0，左上角在屏幕中点
				newWindow.setVisible(true);
				newWindow.setResizable(false);
			} else if (clickedButton == rightBut1) {
				/**
				 * 路经查询按钮
				 */
				card.first(cardPanel);
				for (int i = 0;i < 0;i++) {
					card.next(cardPanel);
				}
				repaint();
			} else if (clickedButton == rightBut2) {
				/**
				 * 景点信息按钮
				 */
				card.first(cardPanel);
				for (int i = 0;i < 1;i++) {
					card.next(cardPanel);
				}
			} else if (clickedButton == rightBut3) {
				/**
				 * 添加节点按钮
				 */
				card.first(cardPanel);
				for (int i = 0;i < 2;i++) {
					card.next(cardPanel);
				}
				repaint();
			} else if (clickedButton == rightBut4) {
				/**
				 * 删除节点按钮
				 */
				card.first(cardPanel);
				for (int i = 0;i < 3;i++) {
					card.next(cardPanel);
				}
				repaint();
			} else if (clickedButton == rightBut5) {
				/**
				 * 添加景点按钮
				 */
				card.first(cardPanel);
				for (int i = 0;i < 4;i++) {
					card.next(cardPanel);
				}
				repaint();
			} else if (clickedButton == rightBut6) {
				/**
				 * 删除景点按钮
				 */
				card.first(cardPanel);
				for (int i = 0;i < 5;i++) {
					card.next(cardPanel);
				}
				repaint();
			} else if (clickedButton == rightBut7) {
				/**
				 * 删除景点按钮
				 */
				card.first(cardPanel);
				for (int i = 0;i < 6;i++) {
					card.next(cardPanel);
				}
			}
		}
		
	}
	
	class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getSource() == thirdPanelUpMapLabel) {
				String x = String.valueOf(e.getX());
				String y = String.valueOf(e.getY());
				thirdPanelDownLab2.setText(x + "," + y);
			}
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getSource() == secondPanelList) {
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
		             String viewName = (String) secondPanelList.getSelectedValue();
		             String introduction = view.get(viewName).split(" ")[1];
		             secondPanelLab.setText(introduction);
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
