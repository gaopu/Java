package org.geekgao.guide;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;


/**
 * 
 * @author geekgao
 * 提供静态的算法方法
 *
 */

public class GuideAlgorithm {
	
	/**
	 * 
	 * @param start	起始点序号
	 * @param end	结束点序号
	 * @param count	点的个数
	 * @param map	图
	 * @return
	 */
	public static Integer[] Dijkstra(int start,int end,int count,Map<Integer,Vertex> map) {
		if (start == end) {
			return null;
		}
		
		class LengthAndRoad{
			int length;
			List<Integer> road = new LinkedList<Integer>();
		}
		LengthAndRoad[] temp= new LengthAndRoad[count + 1];//算法中临时用到的那个数组,数组下标对应节点序号
		Set<Integer> alreadyFind = new HashSet<Integer>();//已经找到最短路径的那些点的集合
		
//		===============================================
//		初始化这个数组
		Vertex tmpVex = map.get(start);
		Set<Integer> point = tmpVex.pointNum.keySet();//这个起始点指向的那些点的序号
		for (Integer i:point) {
			temp[i] = new LengthAndRoad();
			temp[i].length = tmpVex.pointNum.get(i);
			temp[i].road = new LinkedList<Integer>();
			temp[i].road.add(start);
			temp[i].road.add(i);
		}
		alreadyFind.add(start);
//		===============================================
		int currentStart = 0;//每次循环时起始点的序号,循环进去第一步就是选择这个点（路径长度最短的那个点）
		int MAX = 2147483647;
		for (int k = 1;k <= count;k++) {
			long endTime = System.currentTimeMillis();
			
			int minLength = MAX;
			int n = -1;//下面这个temp数组计数
			for (LengthAndRoad l:temp) {
				n++;
				if (l == null || alreadyFind.contains(n)) {
					continue;
				}
				if (minLength > l.length) {
					minLength = l.length;
					currentStart = n;
				}
			}
			if (currentStart == end) {
				break;
			}
			alreadyFind.add(currentStart);
			
			Vertex currentVex = map.get(currentStart);//当前起始点
			List<Integer> currentStartRoad = new LinkedList<Integer>(temp[currentStart].road);//当前路径(把起始点路径复制过来)
			
			Set<Integer> currentPoint = currentVex.pointNum.keySet();//获得当前点指向哪些点
			for (Integer i:currentPoint) {
				if (alreadyFind.contains(i)) {
					continue;
				}
				if ((temp[i] == null) || (temp[currentStart].length + currentVex.pointNum.get(i) < temp[i].length)) {
					LengthAndRoad newRoad = new LengthAndRoad();
					newRoad.length = temp[currentStart].length + currentVex.pointNum.get(i);
					newRoad.road = new LinkedList<Integer>(currentStartRoad);
					newRoad.road.add(i);
					temp[i] = newRoad;
				}
			}
		}
		
		if (currentStart != end) {
			return null;
		}
		
		Integer[] result = new Integer[temp[currentStart].road.size()];
		int n = 0;
		for (Integer i:temp[currentStart].road) {
			result[n++] = i;
		}
		return result;
	}

	public static Integer[] Bfs(int startNum, Map<Integer, Vertex> map,
			Map<String, String> view, Map<Integer, String> viewNumNameMap) {
		
		List<Integer> resultList = new LinkedList<Integer>();
		Queue<Integer> q = new LinkedList<Integer>();
		
		Set<Integer> alreadyFind = new HashSet<Integer>();//存储已经访问过的点
		q.offer(startNum);//放入起始点
		alreadyFind.add(startNum);
		while (!q.isEmpty()) {
			Integer num = q.poll();
			if (viewNumNameMap.containsKey(num)) {
				resultList.add(num);
			}
			Set<Integer> pointNum = map.get(num).pointNum.keySet();
			for (Integer i:pointNum) {
				if (!alreadyFind.contains(i)) {
					q.offer(i);
					alreadyFind.add(i);
				}
			}
		}
		
		Integer[] result = new Integer[resultList.size()];
		for (int i = 0;i < resultList.size();i++) {
			result[i] = resultList.get(i);
		}
		
		return result;
	}

	public static Integer[] Dfs(int startNum, Map<Integer, Vertex> map,
			Map<String, String> view, Map<Integer, String> viewNumNameMap) {
		
		List<Integer> resultList = new LinkedList<Integer>();//算法存储最终结果的链表
		Stack<Integer> s = new Stack<Integer>();//算法中用到的栈
		Set<Integer> alreadyFind = new HashSet<Integer>();//存储已经访问过的点
		
		s.push(startNum);
		alreadyFind.add(startNum);
		while (!s.isEmpty()) {
			int num = s.pop();
			if (viewNumNameMap.containsKey(num)) {
				resultList.add(num);
			}
			Map<Integer,Integer> pointNum = map.get(num).pointNum;
			for (Iterator<Integer> it = pointNum.keySet().iterator();it.hasNext();) {
				int nextNum = it.next();
				if (!alreadyFind.contains(nextNum)) {
					s.push(nextNum);
					alreadyFind.add(nextNum);
				}
			}
		}
		Integer[] result = new Integer[resultList.size()];
		for (int i = 0;i < resultList.size();i++) {
			result[i] = resultList.get(i);
		}
		
		return result;
	}

}
