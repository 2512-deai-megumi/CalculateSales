package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	//商品定義ファイル名
	private static final String FILE_NAME_COMMODITY_LST = "commodity.lst";

	//商品別集計ファイル名
	private static final String FILE_NAME_COMMODITY_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String COMMODITYFILE_NOT_EXIST = "商品定義ファイルが存在しません";
	private static final String COMMODITYFILE_INVALID_FORMAT = "商品定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_CONCECTIVE_NUMBER = "売上ファイル名が連番になっていません";
	private static final String AMOUNT_OVER = "合計金額が10桁を超えました";
	private static final String CODE_INVALID_FORMAT = "の支店コードが不正です";
	private static final String COMMODITYCODE_INVALID_FORMAT = "の商品コードが不正です";
	private static final String SALESFILE_INVALID_FORMAT = "のフォーマットが不正です";


	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		//引数の確認
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();
		//商品コードと商品名を保持するMap
		Map<String, String> commodityNames = new HashMap<>();
		//商品コードと売り上げ金額を保持するMap
		Map<String, Long> commoditySales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		//商品定義ファイル読み込み処理
		if(!readCommodityFile(args[0], FILE_NAME_COMMODITY_LST, commodityNames, commoditySales)) {
			return;
		}


		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)


		//ファイルパス
		String path = args[0];
		//一時的にファイル名を保管する配列
		File[] allFiles = new File(path).listFiles();
		//rcdファイル名を保管するリスト
		List<File> rcdFiles = new ArrayList<>();


		for(int i = 0; i < allFiles.length; i++) {
			String rcdFileName = allFiles[i].getName();
			if(allFiles[i].isFile() && rcdFileName.matches("^[0-9]{8}.rcd$")) {
				 rcdFiles.add(allFiles[i]);
			}
		}

		//ファイル名が連番になっているのか確認
		Collections.sort(rcdFiles);
		for(int i = 0;i < rcdFiles.size() - 1; i++) {
			String formerFile = rcdFiles.get(i).getName();
			String latterFile = rcdFiles.get(i + 1).getName();
			int formerNum =  Integer.parseInt(formerFile.substring(0, 8));
			int latterNum =  Integer.parseInt(latterFile.substring(0, 8));

			if(latterNum - formerNum != 1) {
				System.out.println(FILE_NOT_CONCECTIVE_NUMBER);
				return;
			}

		}

		//金額の集計
		for(int i = 0;i < rcdFiles.size(); i++) {
			BufferedReader br = null;
			try {
				String fileName = rcdFiles.get(i).getName();
				File rcdFile = new File(path, fileName);
				FileReader fr = new FileReader(rcdFile);
				br = new BufferedReader(fr);
				String line;
				List<String> salesList = new ArrayList<String>();

				while((line = br.readLine()) != null) {
					salesList.add(line);
				}

				//売り上げファイルのフォーマットを確認
				if(salesList.size() != 3) {
					System.out.println(fileName + SALESFILE_INVALID_FORMAT);
					return;
				}

				//支店コードが有効か確認
				if(!branchSales.containsKey(salesList.get(0))) {
					System.out.println(fileName + CODE_INVALID_FORMAT);
					return;
				}

				//商品コードが有効か確認
				if(!commoditySales.containsKey(salesList.get(1))) {
					System.out.println(fileName + COMMODITYCODE_INVALID_FORMAT);
					return;
				}

				//売り上げ金額が数字なのか確認
				if(!salesList.get(2).matches("[0-9]+")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}
				//支店別の売り上げ金額を計算
				String code = salesList.get(0);

				Long sales = Long.parseLong(salesList.get(2));
				Long salesAmount = branchSales.get(code) + sales;

				//支店別の売り上げの合計金額が10桁を超えていないか確認
				if(salesAmount > 10000000000L) {
					System.out.println(AMOUNT_OVER);
					return;
				}

				branchSales.replace(code, salesAmount);

				//商品別の売り上げ金額を計算
				String commodityCode = salesList.get(1);

				Long commodityAmount = commoditySales.get(commodityCode) + sales;

				//商品別の売り上げの合計金額が10桁を超えていないか確認
				if(commodityAmount > 10000000000L) {
					System.out.println(AMOUNT_OVER);
					return;
				}

				commoditySales.replace(commodityCode, commodityAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODITY_OUT, commodityNames, commoditySales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */

	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			//ファイルが存在するのか確認
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] branches = line.split(",");


				//支店定義ファイルのフォーマットを確認
				if((branches.length != 2) || (!branches[0].matches("[0-9]{3}"))) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}
				String code = branches[0];
				String branch = branches[1];


				branchNames.put(code, branch);
				branchSales.put(code, (long) 0);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 商品定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */

	private static boolean readCommodityFile(String path, String fileName, Map<String, String> commodityNames, Map<String, Long> commoditySales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			//ファイルが存在するのか確認
			if(!file.exists()) {
				System.out.println(COMMODITYFILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] commodities = line.split(",");


				//商品定義ファイルのフォーマットを確認
				if(( commodities.length != 2) || (! commodities[0].matches("[0-9a-zA-Z]{8}"))) {
					System.out.println(COMMODITYFILE_INVALID_FORMAT);
					return false;
				}
				String code =  commodities[0];
				String commodity =  commodities[1];


				commodityNames.put(code, commodity);
				commoditySales.put(code, (long) 0);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コード(商品コード)と支店名を保持するMap
	 * @param 支店コード(商品コード)と売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> Names, Map<String, Long> Sales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		File file = new File(path, fileName);
		BufferedWriter bw = null;

		try {
			FileWriter filewriter = new FileWriter(file);
			bw = new BufferedWriter(filewriter);
			//出力ファイルに書き込む
			for(String key: Names.keySet()) {
				bw.write(key + "," + Names.get(key) + "," + Sales.get(key));
				bw.newLine();
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			if(bw != null) {
				try{
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}


		return true;
	}



}
