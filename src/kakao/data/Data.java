package kakao.data;
import kakao.data.SparseRow;

import org.la4j.matrix.sparse.CRSMatrix;
import org.la4j.vector.dense.BasicVector;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;


public class Data {

	protected int cache_size;
	protected boolean has_xt;
	protected boolean has_x;
	
	public CRSMatrix data;
	public CRSMatrix data_t;
	public List<SparseRow> sparseData;
	public BasicVector target;
	public int numRows;	// num of rows
	public int numCols;	 // num of columns 
	public double minTarget;
	public double maxTarget;

	public Data(int cache_size, boolean has_x, boolean has_xt) {
		this.data = null;
		this.data_t = null;
		this.sparseData = null;
		this.cache_size = cache_size;
		this.has_x = has_x;
		this.has_xt = has_xt;
		this.target = null;
		this.numRows = 0;
		this.numCols = 0;
		this.minTarget = Double.MAX_VALUE;
		this.maxTarget = -Double.MAX_VALUE;
	}
	
	
	public void load(String filename) throws IOException {
		System.out.println("has x = " + has_x);
		System.out.println("has xt = " + has_xt);
		int numFeature = 0;
		int numValues = 0;
		
		// (1) Determine the number of rows and the maximum feature_id
		try {
			BufferedReader fData = new BufferedReader(new FileReader(filename));
			StringTokenizer st;
			String line, curr;
			String[] pair;
			int currFeatureId;
			double currTarget;
			while ((line = fData.readLine()) != null) {
                numRows++;
				st = new StringTokenizer(line);
				while (st.hasMoreTokens()) {
					curr = st.nextToken();
					if (isDouble(curr)) {
						currTarget = Double.parseDouble(curr);
						minTarget = Math.min(currTarget, minTarget);
						maxTarget = Math.max(currTarget, maxTarget);
						// debug
						System.out.println("numRows=" + numRows + "\tcurrTarget=" + currTarget + "\tminTarget=" + minTarget + "\tmaxTarget=" + maxTarget);
						// /debug
					} else if (curr.matches("\\d+:\\d+")) {
						numValues++;
						pair = curr.split(":");
						currFeatureId = Integer.parseInt(pair[0]);
						numFeature = Math.max(currFeatureId, numFeature);
						// debug
						System.out.println("currFeatureId=" + currFeatureId + "\tnumFeature=" + numFeature + "\tnumValues=" + numValues);
						// /debug
					}
				}
			}
			fData.close();
		} catch (IOException e) {
			System.out.println("unable to open " + filename);
		}
		
		this.numCols = numFeature;
		this.data = new CRSMatrix(numRows+1, numCols+1);
		this.target = new BasicVector(numRows+1);
		sparseData = new ArrayList<SparseRow>(numRows+1);
		for (int i = 0; i <= numRows+1; i++) { sparseData.add(null); }

		
		// (2) Read the data
		try {
			BufferedReader fData = new BufferedReader(new FileReader(filename));
			StringTokenizer st;
			String line, curr;
			String[] pair;
			int currFeatureId;
			double currTarget, currFeatureValue;
			int rowId = 0;
			while ((line = fData.readLine()) != null) {
                rowId++;
				st = new StringTokenizer(line);
                sparseData.set(rowId, new SparseRow());
				while (st.hasMoreTokens()) {
					curr = st.nextToken();
					if (isDouble(curr)) {
						currTarget = Double.parseDouble(curr);
						target.set(rowId, currTarget);
						// debug
						System.out.println("rowId=" + rowId + "\tcurrTarget=" + currTarget);
						// /debug
					} else if (curr.matches("\\d+:\\d+")) {
						pair = curr.split(":");
						currFeatureId = Integer.parseInt(pair[0]);
						currFeatureValue = Double.parseDouble(pair[1]);
						sparseData.set(rowId, sparseData.get(rowId).add(currFeatureId, currFeatureValue));
						data.set(rowId, currFeatureId, currFeatureValue);
						// debug
						System.out.println("currFeatureId=" + currFeatureId + "\tcurrFeatureValue=" + currFeatureValue);
						// /debug
					}
				}
			}
			fData.close();
		} catch (IOException e) {
			System.out.println("unable to open " + filename);
		}

		System.out.println("numRows=" + numRows + "\tnumCols=" + numCols + "\tminTarget=" + minTarget + "\tmaxTarget=" + maxTarget);

	}

	private boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
