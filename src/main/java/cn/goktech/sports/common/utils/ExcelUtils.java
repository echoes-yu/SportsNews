package cn.goktech.sports.common.utils;



import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * excel导入导出工具类
 * 
 * @author ljc
 */
public class ExcelUtils {

	private final static Logger log = LoggerFactory.getLogger(ExcelUtils.class);

	private final static String EXCEL2003 = "xls";
	private final static String EXCEL2007 = "xlsx";

	/**
	 * 导入excel数据
	 * 
	 * @param cls
	 *            导入数据的实体 类对象
	 * @param file
	 *            上传文件
	 * @param <T>
	 * @return 返回实体对象 List
	 */
	public static <T> List<T> readExcel(Class<T> cls, MultipartFile file) {
		String fileName = file.getOriginalFilename();
		if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
			log.error("上传文件格式不正确");
		}
		List<T> dataList = new ArrayList<>();
		Workbook workbook = null;
		try {
			InputStream is = file.getInputStream();
			if (fileName.endsWith(EXCEL2007)) {
				// FileInputStream is = new FileInputStream(new File(path));
				workbook = new XSSFWorkbook(is);
			}
			if (fileName.endsWith(EXCEL2003)) {
				// FileInputStream is = new FileInputStream(new File(path));
				workbook = new HSSFWorkbook(is);
			}
			if (workbook != null) {
				// 类映射 注解 value-->bean columns
				Map<String, List<Field>> classMap = new HashMap<>();
				List<Field> fields = Stream.of(cls.getDeclaredFields()).collect(Collectors.toList());
				fields.forEach(field -> {
					ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
					if (annotation != null) {
						String value = annotation.value();
						if (isBlank(value)) {
							return;// return起到的作用和continue是相同的 语法
						}
						if (!classMap.containsKey(value)) {
							classMap.put(value, new ArrayList<>());
						}
						field.setAccessible(true);
						classMap.get(value).add(field);
					}
				});
				// 索引-->列与被注解属性的对应关系
				Map<Integer, List<Field>> reflectionMap = new HashMap<>(16);
				// 默认读取第一个sheet
				Sheet sheet = workbook.getSheetAt(0);

				boolean firstRow = true;
				for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
					Row row = sheet.getRow(i);
					// 首行 提取注解
					if (firstRow) {
						for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
							Cell cell = row.getCell(j);
							String cellValue = getCellValue(cell);
							if (classMap.containsKey(cellValue)) {
								reflectionMap.put(j, classMap.get(cellValue));
							}
						}
						firstRow = false;
					} else {
						// 忽略空白行
						if (row == null) {
							continue;
						}
						try {
							T t = cls.newInstance();
							// 判断是否为空白行
							boolean allBlank = true;
							for (int j = row.getFirstCellNum(); j <= row.getLastCellNum(); j++) {
								if (reflectionMap.containsKey(j)) {
									Cell cell = row.getCell(j);
									String cellValue = getCellValue(cell);
									if (!isBlank(cellValue)) {
										allBlank = false;
									}
									List<Field> fieldList = reflectionMap.get(j);
									fieldList.forEach(x -> {
										try {
											handleField(t, cellValue, x);
										} catch (Exception e) {
											log.error(String.format("设置 属性:%s 值:%s 时出现错误!", x.getName(), cellValue), e);
										}
									});
								}
							}
							if (!allBlank) {
								dataList.add(t);
							} else {
								log.warn(String.format("第%s行有空单元格", i));
							}
						} catch (Exception e) {
							log.error(String.format("导入第%s行出现错误", i), e);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(String.format("导出excel出现错误"), e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (Exception e) {
					log.error(String.format("导入失败"), e);
				}
			}
		}
		return dataList;
	}

	/**
	 * 给对象属性赋值
	 * 
	 * @param t
	 *            对象
	 * @param value
	 *            值
	 * @param field
	 *            属性
	 * @param <T>
	 * @throws Exception
	 */
	private static <T> void handleField(T t, String value, Field field) throws Exception {
		Class<?> type = field.getType();
		if (type == null || type == void.class || isBlank(value)) {
			return;
		}
		if (type == Object.class) {
			field.set(t, value);
			// 数字类型
		} else if (type.getSuperclass() == null || type.getSuperclass() == Number.class) {
			if (type == int.class || type == Integer.class) {
				field.set(t, new Integer(value));
			} else if (type == long.class || type == Long.class) {
				field.set(t, new Long(value));
			} else if (type == byte.class || type == Byte.class) {
				field.set(t, new Byte(value));
			} else if (type == short.class || type == Short.class) {
				field.set(t, new Short(value));
			} else if (type == double.class || type == Double.class) {
				field.set(t, new Double(value));
			} else if (type == float.class || type == Float.class) {
				field.set(t, new Float(value));
			} else if (type == char.class || type == Character.class) {
				field.set(t, value.charAt(0));
			} else if (type == boolean.class) {
				field.set(t, new Boolean(value));
			} else if (type == BigDecimal.class) {
				field.set(t, new BigDecimal(value));
			}
		} else if (type == Boolean.class) {
			field.set(t, new Boolean(value));
		} else if (type == Date.class) {
			//
			field.set(t, value);
		} else if (type == String.class) {
			field.set(t, value);
		} else {
			Constructor<?> constructor = type.getConstructor(String.class);
			field.set(t, constructor.newInstance(value));
		}
	}

	private static String getCellValue(Cell cell) {
		if (cell == null) {
			return "";
		}
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			if (DateUtil.isCellDateFormatted(cell)) {
				return HSSFDateUtil.getJavaDate(cell.getNumericCellValue()).toString();
			} else {
				return new BigDecimal(cell.getNumericCellValue()).toString();
			}
		} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return cell.getStringCellValue().trim();
		} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			return cell.getCellFormula().trim();
		} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return "";
		} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(cell.getBooleanCellValue());
		} else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
			return "ERROR";
		} else {
			return cell.toString().trim();
		}
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param s
	 * @return
	 */
	private static boolean isBlank(String s) {
		if (s == null) {
			return true;
		} else if (s.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 将List生成excel表格
	 * 
	 * @param dataList
	 *            源数据
	 * @param cls
	 *            数据实体 类对象
	 * @param <T>
	 * @return excel对象(Workbook)
	 */
	public static <T> Workbook writeExcel(List<T> dataList, Class<T> cls) {
		Field[] fields = cls.getDeclaredFields();
		List<Field> fieldList = Arrays.stream(fields) // 将数组转换为流
				.filter(field -> { // 过滤掉注解中没有所需内容的属性
					ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
					if (annotation != null && annotation.col() > 0) {
						field.setAccessible(true);
						return true;
					}
					return false;
				}).sorted(Comparator.comparing(field -> { // 根据注解中col值排序
					int col = 0;
					ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
					if (annotation != null) {
						col = annotation.col();
					}
					return col;
				})).collect(Collectors.toList()); // 将流转换为List

		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");
		AtomicInteger ai = new AtomicInteger(); // 行
		{
			Row row = sheet.createRow(ai.getAndIncrement());
			AtomicInteger aj = new AtomicInteger(); // 列
			// 写入表头
			fieldList.forEach(field -> {
				ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
				String columnName = "";
				if (annotation != null) {
					columnName = annotation.value();
				}
				Cell cell = row.createCell(aj.getAndIncrement());

				CellStyle cellStyle = wb.createCellStyle();
				cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
				cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

				Font font = wb.createFont();
				font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
				cellStyle.setFont(font);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(columnName);
			});
		}
		// 写入数据
		if (dataList != null) {
			dataList.forEach(t -> {
				Row row1 = sheet.createRow(ai.getAndIncrement());
				AtomicInteger aj = new AtomicInteger();
				fieldList.forEach(field -> {
					Class<?> type = field.getType();
					Object value = "";
					try {
						value = field.get(t); // 从对象中获取该属性值
					} catch (Exception e) {
						e.printStackTrace();
					}
					Cell cell = row1.createCell(aj.getAndIncrement());
					if (value != null) {
						if (type == Date.class) {
							cell.setCellValue(value.toString());
						} else {
							cell.setCellValue(value.toString());
						}
						cell.setCellValue(value.toString());
					}
				});
			});
		}
		// 冻结窗格
		wb.getSheet("Sheet1").createFreezePane(0, 1, 0, 1);

		return wb;
	}

	/**
	 * 浏览器下载excel
	 * 
	 * @param fileName
	 *            给文件命名
	 * @param wb
	 *            excel对象
	 * @param response
	 */

	public static void buildExcelDocument(String fileName, Workbook wb, HttpServletResponse response) {
		try {
			response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
			response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
			response.flushBuffer();
			wb.write(response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成excel文件到本地
	 * 
	 * @param path
	 *            生成excel路径
	 * @param wb
	 */
	public static void buildExcelFile(String path, Workbook wb) {

		File file = new File(path);
		if (!file.exists()) {
			// 没有就创建文件夹
			file.mkdirs();
		}
		if (file.exists()) {
			file.delete();
		}
		try {
			wb.write(new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 商品导出
	public static void createExcel(Map<String, List<String>> map, String[] strArray, String filePath) {
		// 第一步，创建一个webbook，对应一个Excel文件
		HSSFWorkbook wb = new HSSFWorkbook();
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet("sheet1");
		sheet.setDefaultColumnWidth(20);// 默认列宽
		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
		HSSFRow row = sheet.createRow((int) 0);
		// 第四步，创建单元格，并设置值表头 设置表头居中
		HSSFCellStyle style = wb.createCellStyle();
		// 创建一个居中格式
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		// 添加excel title
		HSSFCell cell = null;
		for (int i = 0; i < strArray.length; i++) {
			cell = row.createCell((short) i);
			cell.setCellValue(strArray[i]);
			cell.setCellStyle(style);
		}

		// 第五步，写入实体数据 实际应用中这些数据从数据库得到,list中字符串的顺序必须和数组strArray中的顺序一致
		int i = 0;
		for (String str : map.keySet()) {
			row = sheet.createRow((int) i + 1);
			List<String> list = map.get(str);

			// 第四步，创建单元格，并设置值
			for (int j = 0; j < strArray.length; j++) {
				row.createCell((short) j).setCellValue(list.get(j));
			}
			i++;
		}
		// 第六步，将文件存到指定位置
		try {
			File file = new File("C:/ExcelFiles");
			// 如果文件夹不存在则创建
			if (!file.exists() && !file.isDirectory()) {
				System.out.println("//不存在");
				file.mkdir();
			} else {
				System.out.println("//目录存在");
			}
			FileOutputStream fout = new FileOutputStream(filePath);
			wb.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 柜机模板导出
	 */
	public static void createExcel(Map<String, List<String>> map, String[] strArray) {
		// 第一步，创建一个webbook，对应一个Excel文件
		HSSFWorkbook wb = new HSSFWorkbook();
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet("sheet1");
		sheet.setDefaultColumnWidth(20);// 默认列宽
		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
		HSSFRow row = sheet.createRow((int) 0);
		// 第四步，创建单元格，并设置值表头 设置表头居中
		HSSFCellStyle style = wb.createCellStyle();
		// 创建一个居中格式
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		// 添加excel title
		HSSFCell cell = null;
		for (int i = 0; i < strArray.length; i++) {
			cell = row.createCell((short) i);
			cell.setCellValue(strArray[i]);
			cell.setCellStyle(style);
		}

		// 第五步，写入实体数据 实际应用中这些数据从数据库得到,list中字符串的顺序必须和数组strArray中的顺序一致
		int i = 0;
		for (String str : map.keySet()) {
			row = sheet.createRow((int) i + 1);
			List<String> list = map.get(str);

			// 第四步，创建单元格，并设置值
			for (int j = 0; j < strArray.length; j++) {
				row.createCell((short) j).setCellValue(list.get(j));
			}

			// 第六步，将文件存到指定位置
			try {
				File file = new File("C:/template");
				// 如果文件夹不存在则创建
				if (!file.exists() && !file.isDirectory()) {
					System.out.println("//不存在");
					file.mkdir();
				} else {
					System.out.println("//目录存在");
				}
				FileOutputStream fout = new FileOutputStream("C:/template/导入Excel模板.xls");
				wb.write(fout);
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
	}

	/**
	 * 导出Excel
	 * 
	 * @param sheetName
	 *            sheet名称
	 * @param title
	 *            标题
	 * @param values
	 *            内容
	 * @param wb
	 *            HSSFWorkbook对象
	 * @return
	 */
	public static HSSFWorkbook getHSSFWorkbook(String sheetName, String[] title, String[][] values, HSSFWorkbook wb) {

		// 第一步，创建一个HSSFWorkbook，对应一个Excel文件
		if (wb == null) {
			wb = new HSSFWorkbook();
		}

		// 第二步，在workbook中添加一个sheet,对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet(sheetName);

		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制
		HSSFRow row = sheet.createRow(0);

		/*
		 * // 第四步，创建单元格，并设置值表头 设置表头居中 HSSFCellStyle style =
		 * wb.createCellStyle(); style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		 * // 创建一个居中格式
		 */
		// 声明列对象
		HSSFCell cell = null;

		// 创建标题
		for (int i = 0; i < title.length; i++) {
			cell = row.createCell(i);
			cell.setCellValue(title[i]);
			/* cell.setCellStyle(style); */
		}

		// 创建内容
		for (int i = 0; i < values.length; i++) {
			row = sheet.createRow(i + 1);
			for (int j = 0; j < values[i].length; j++) {
				// 将内容按顺序赋给对应的列对象
				row.createCell(j).setCellValue(values[i][j]);
			}
		}
		return wb;
	}

	/**
	 * 读入excel文件，解析后返回
	 * @param file
	 * @throws IOException
	 */
	public List<String[]> readExcel(MultipartFile file) throws IOException {
		//检查文件
		//checkFile(file);
		//获得Workbook工作薄对象
		Workbook workbook = getWorkBook(file);
		//创建返回对象，把每行中的值作为一个数组，所有行作为一个集合返回
		List<String[]> list = new ArrayList<String[]>();
		if(workbook != null){
			for(int sheetNum = 0;sheetNum < workbook.getNumberOfSheets();sheetNum++){
				//获得当前sheet工作表
				Sheet sheet = workbook.getSheetAt(sheetNum);
				if(sheet == null){
					continue;
				}
				//获得当前sheet的开始行
				int firstRowNum  = sheet.getFirstRowNum();
				//获得当前sheet的结束行
				int lastRowNum = sheet.getLastRowNum();
				//循环除了第一行的所有行
				for(int rowNum = firstRowNum+1;rowNum <= lastRowNum;rowNum++){
					//获得当前行
					Row row = sheet.getRow(rowNum);
					if(row == null){
						continue;
					}
					//获得当前行的开始列
					int firstCellNum = row.getFirstCellNum();
					//获得当前行的列数
					int lastCellNum = row.getPhysicalNumberOfCells();
					String[] cells = new String[row.getPhysicalNumberOfCells()];
					//循环当前行
					for(int cellNum = firstCellNum; cellNum < lastCellNum;cellNum++){
						Cell cell = row.getCell(cellNum);
						cells[cellNum] = getCellValue(cell);
					}
					list.add(cells);
				}
			}
			workbook.close();
		}
		return list;
	}

	public static Workbook getWorkBook(MultipartFile file) {
		//获得文件名
		String fileName = file.getOriginalFilename();
		//创建Workbook工作薄对象，表示整个excel
		Workbook workbook = null;
		try {
			//获取excel文件的io流
			InputStream is = file.getInputStream();
			//根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
			if(fileName.endsWith(EXCEL2003)){
				//2003
				workbook = new HSSFWorkbook(is);
			}else if(fileName.endsWith(EXCEL2007)){
				//2007
				workbook = new XSSFWorkbook(is);
			}
		} catch (IOException e) {
			//Logger.info(e.getMessage());
		}
		return workbook;
	}



}
