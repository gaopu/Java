package org.geekgao.guide;

import java.util.Map;

/**
 * 
 * @author geekgao
 * 导游系统的数据结构
 * 表示图中的一个点
 * 包权限
 *
 */

class Vertex {
	int num;//编号
	int x,y;//坐标
	Map<Integer,Integer> pointNum;//指向哪些点(点的编号和weight)
}
