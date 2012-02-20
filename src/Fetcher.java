import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;


public class Fetcher {

	Cache c;
	PostgresConnector pgc;
	Set closed;
	DataOutputStream f ;
	
	Vector <HashSet>rawGraph;
	
	public Fetcher(int size) throws ClassNotFoundException, SQLException, FileNotFoundException{
		closed = new HashSet();
		c = new Cache();								
		pgc = new PostgresConnector();
		
		rawGraph = new Vector();
		
		int x=1;
			System.out.println(x);
			int uplimit = x*size;
			int downlimit = (x-1)*size; 
			String sql = "SELECT * from p2 where (j_from <"+uplimit+"AND j_from >= "+ downlimit+") OR (j_to <"+uplimit+"AND j_to >= "+ downlimit+")";
			
		
			ResultSet rs = pgc.query(sql);
		
			while(rs.next()){

				int j_from = rs.getInt("j_from");
				int j_to = rs.getInt("j_to");

				if(c.idPositionMap.get(j_from) == null){
					c.idPositionMap.put(j_from, rawGraph.size());
					
					rawGraph.add(new HashSet());
				}
				
				if(c.idPositionMap.get(j_to) == null){
					c.idPositionMap.put(j_to, rawGraph.size());
				
					rawGraph.add(new HashSet());
				}
				
				rawGraph.get(c.idPositionMap.get(j_from)).add(j_to);
				rawGraph.get(c.idPositionMap.get(j_to)).add(j_from);

			}
		
	}
	
	public void dumbload() throws IOException{
		System.out.println("Key layout..");

		Set ids = new HashSet(c.idPositionMap.keySet());
		Iterator <Integer>it = ids.iterator();
		while(it.hasNext()){
			
		int tmp = it.next();
			c.assignNeighbour(tmp, rawGraph.get(c.idPositionMap.get(tmp)));
		}
		
		
		c.commit();

		

	}
	
	public void loadFetcher(int id, int l1, int l2, int l3, int l4) throws SQLException, IOException{
		System.out.println("H-Block layout..");
		
		double [] s = new double [5];
		int max =4;
		
		s[0] = l1;
		s[1] = l2;
		s[2] = l3;
		s[3] = l4;
		s[4] = Double.POSITIVE_INFINITY;	
		
		Deque <Integer> [] roots = new ArrayDeque [5];
		Deque <Integer> [] leaves = new ArrayDeque [5];
		double [] space = new double [5];
		
		for(int x=0; x<5; x++){
			roots[x] = new ArrayDeque();
			leaves[x] = new ArrayDeque();
		}
		
		Integer initialVertice = id;
		
		int level = max;
		
		roots[max].offerLast(initialVertice);
		
		
		while(true){											// assumes infinite memory

//			System.out.println(leaves[level]+" "+level);
			

			if(roots[level].isEmpty()){
				
				roots[level].addAll(leaves[level]);
				leaves[level].clear();							//needs review
			
				if(space[level] >= s[level] ){
					leaves[level+1].addAll(roots[level]); 		//needs review
					roots[level].clear();
					space[level+1] += space[level];
					level++;
					continue;
				}
			}
			
			if(roots[level].isEmpty()){
				
				if(level == max){
					break;
				}
				else{
					space[level+1] += space[level];
					level++;
				}
			}
			else{
				int v = roots[level].pollFirst();
				
				if(level>0){
					roots[level-1].offerLast(v);
					space[level-1] = 0;
					level--;
				}
				else{
					Vector children = getChildrenOfVertice(v);
					leaves[0].addAll(children);
					space[0] += children.size()*4+4;					
				}
			}
		}
	
		c.commit();

	}
	

	public Vector getChildrenOfVertice(int v) throws SQLException, IOException{
		closed.add(v);

		
		int j_from = v;
		
		
		Vector neighboursToExplore = new Vector();
		
		HashSet allNeighbours = rawGraph.get(c.idPositionMap.get(v));
		
	
		Iterator <Integer>it = allNeighbours.iterator();
		
		while(it.hasNext()){
			int child = it.next();
			

			//System.out.println(j_from+"\t"+child);
				
			if(!closed.contains(child)){
				closed.add(child);
				neighboursToExplore.add(child);
			}
		}
		
		c.assignNeighbour(j_from, allNeighbours);
		
		return neighboursToExplore;
	}
	
	
	public void BFSBlock() throws IOException{
		
		System.out.println("BFS layout");

		Deque <Integer>q = new ArrayDeque();
		Set visited = new HashSet();
		
		int initialVertice = 0;
		
		q.addLast(initialVertice);
		visited.add(initialVertice);
		
		while(!q.isEmpty()){
			

			int tmp = q.pollFirst();
//			System.out.println(tmp);


//			if(tmp == end)
//				break;
			//can have an exit condition here
			
			Set children = rawGraph.get(c.idPositionMap.get(tmp));
			c.assignNeighbour(tmp, children);

			Iterator <Integer>it = children.iterator();
			
			while(it.hasNext()){
				int o = it.next();

				if(!visited.contains(o)){
					visited.add(o);
					q.addLast(o);
				}
			}

		}
		c.commit();
		
	}
	
	
	public int BFS(int start, int end){
		System.out.println(c.edgeList.toString());
		Deque <Integer>q = new ArrayDeque();
		Set visited = new HashSet();
		
		int initialVertice = start;
		
		q.addLast(initialVertice);
		visited.add(initialVertice);
		
		while(!q.isEmpty()){
			

			int tmp = q.pollFirst();

			if(tmp == end)
				break;
			//can have an exit condition here
			Vector <Integer>neighbours = c.getNeighbours(tmp);

			for(int x=0; x<neighbours.size(); x++){
				int o = neighbours.get(x);

				if(!visited.contains(o)){
					visited.add(o);
					q.addLast(o);
				}
			}

		}
		
		return visited.size();
	}
	
	
	
	public int unformatedBFS(int start, int end){
		Deque <Integer>q = new ArrayDeque();
		Set visited = new HashSet();
		
		int initialVertice = 0;
		
		q.addLast(initialVertice);
		visited.add(initialVertice);
		
		while(!q.isEmpty()){
			

			int tmp = q.pollFirst();
			
			if(tmp==end)
				break;

			HashSet allNeighbours = rawGraph.get(c.idPositionMap.get(tmp));
			
			Iterator <Integer>it = allNeighbours.iterator();

			while(it.hasNext()){
				int o = it.next();

				if(!visited.contains(o)){
					visited.add(o);
					q.addLast(o);
				}
			}

		}
		
		return visited.size();
	}
}


