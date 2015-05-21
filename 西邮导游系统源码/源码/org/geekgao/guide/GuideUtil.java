package org.geekgao.guide;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * 
 * @author geekgao
 * 工具类
 * 读入图片,读取文件等
 *
 */
public class GuideUtil {
	
    private GuideUtil(){}

    /**
     * 
     * @param path
     * @return
     * 读入图片
     */
    public static Image getImage(String path) {
        URL u = GuideUtil.class.getClassLoader().getResource(path);
        BufferedImage img = null;

        try {
            img = ImageIO.read(u);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }
    
    /**
     * 
     * @param path
     * @return
     * 读入节点信息
     */
    public static Map<Integer,Vertex> getVertex(String path) {
    	Map<Integer,Vertex> map = new HashMap<Integer, Vertex>();
    	BufferedReader fileIn = null;    	
    	String lineStr;
    	
    	try {
    		fileIn = new BufferedReader(new FileReader(path));
			while ((lineStr = fileIn.readLine()) != null) {
				Vertex vex = new Vertex();
				String[] everyNum = lineStr.split(" ");
				vex.num = Integer.parseInt(everyNum[0]);
				vex.x = Integer.parseInt(everyNum[1]);
				vex.y = Integer.parseInt(everyNum[2]);
				
				vex.pointNum = new HashMap<Integer, Integer>();
				for (int i = 3;i < everyNum.length;i+=2) {
					vex.pointNum.put(Integer.valueOf(everyNum[i]), Integer.valueOf(everyNum[i+1]));
				}
				map.put(vex.num, vex);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	
		return map;
		
    }
    
    public static void setVertex(Map<Integer,Vertex> map,String outPath) {
    	BufferedWriter outFile = null;
    	Set<Integer> vertexNum = map.keySet();
    	
    	try {
			outFile = new BufferedWriter(new FileWriter(outPath));
			for (Integer v:vertexNum) {
				Vertex tmpVex = map.get(v);
				outFile.write(v + " " + tmpVex.x + " " + tmpVex.y);
				Map<Integer,Integer> tmpLength = tmpVex.pointNum;
				Set<Integer> pointNum = tmpLength.keySet();
				for (Integer i:pointNum) {
					outFile.write(" " + i + " " + tmpLength.get(i));
				}
				outFile.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
    public static Map<String,String> getView(String path) {
    	Map<String,String> result = new HashMap<String, String>();
    	BufferedReader fileIn = null;
    	
    	try {
			fileIn = new BufferedReader(new FileReader(path));
			String all,first,second;
			String[] tmp;
			while ((all = fileIn.readLine()) != null) {
				tmp = all.split(" ", 2);
				first = tmp[0];
				second = tmp[1];
				result.put(first, second);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fileIn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	
		return result;
    }
    
    public static void setView(Map<String,String> view,String outPath) {
    	BufferedWriter outFile = null;
    	Set<String> viewName = view.keySet();
    	
    	try {
			outFile = new BufferedWriter(new FileWriter(outPath));
			for (String v:viewName) {
				outFile.write(v + " " + view.get(v));
				outFile.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
    
}