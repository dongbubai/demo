package com.mtpc.admin.controller.exportSql;
    
import java.awt.Color; 
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List; 
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
 
public class ExportSqlController extends JFrame implements ActionListener{
 
    private static Connection conn = null;
    private static Statement sm = null;
//    private static String schema = "motooling2";//模式名
    private static String select = "SELECT * FROM";//查询sql
    private static String insert = "INSERT INTO";//插入sql
    private static String values = "VALUES";//values关键字
    private String[] table = {"sm_funcregister","sm_butnregister","sm_menuitemreg","pub_systemplate_base",
    	"pub_billtemplet","pub_billtemplet_b","pub_billtemplet_t","muap_bill_h","muap_bill_b","muap_billyq_b"};//table数组 
   
    private static String filePath = "";//绝对路径导出数据的文件
    String pre = "";
 
    private static final long serialVersionUID = 1L; 
    JButton btn,btn2,btn3 = null; 
    JTextField textField, textField3,textField4,textField5,textField6,textField7 = null;
    JTextArea  textField2, textField8 = null;
    JCheckBox box = new JCheckBox();
    
    /**
     * 导出数据库表*@paramargs *@throwsSQLException
     */
    public static void main(String[] args) throws SQLException {
    	new ExportSqlController(); 
    }
 
    public ExportSqlController() throws SQLException{
//    	this.setTitle("选择文件窗口");
//        FlowLayout layout = new FlowLayout();// 布局
    	JFrame frame=new JFrame("慧都智连-导出工具");
        GridBagLayout gbaglayout=new GridBagLayout();    //创建GridBagLayout布局管理器
        GridBagConstraints constraints=new GridBagConstraints();
        frame.setLayout(gbaglayout);    //使用GridBagLayout布局管理器
        constraints.fill=GridBagConstraints.BOTH;    //组件填充显示区域
        constraints.weightx=0.0;    //恢复默认值
        constraints.gridwidth = GridBagConstraints.REMAINDER;    //结束行
        constraints.weightx=0.5;    // 指定组件的分配区域
        constraints.weighty=0.2;
        constraints.gridwidth=1;
        
        JLabel labe3 = new JLabel("oracle数据库地址：");// 标签
        textField3 = new JTextField(20);// 文本域 
        JLabel labe4 = new JLabel("oracle数据库端口：");// 标签
        textField4 = new JTextField(20);// 文本域 
        JLabel labe5 = new JLabel("oracle数据库实例：");// 标签
        textField5 = new JTextField(20);// 文本域 
        JLabel labe6 = new JLabel("oracle数据库用户：");// 标签
        textField6 = new JTextField(20);// 文本域 
        JLabel labe7 = new JLabel("oracle数据库密码：");// 标签
        textField7 = new JTextField(20);// 文本域 
        
        JLabel label = new JLabel("选择导出脚本路径：");// 标签
        textField = new JTextField(30);// 文本域
       
        
        JLabel labe2 = new JLabel("制单节点-功能注册编码,以,逗号分隔：");// 标签
        textField2 = new JTextArea(3,35);// 文本域 
        textField2.setLineWrap(true);  
        JScrollPane jsp2=new JScrollPane(textField2);
        
        JLabel labe8 = new JLabel("审批节点-交易类型编码,以,逗号分隔：");// 标签
        textField8 = new JTextArea(3,35);// 文本域 
        textField8.setLineWrap(true); 
        JScrollPane jsp8=new JScrollPane(textField8); 
        
 
        // 设置布局
//        this.setLayout(new FlowLayout(FlowLayout.LEFT,10,5));// 左对齐
//        this.setLayout(new BorderLayout());   
//        this.setLayout(layout);
//        this.setBounds(400, 200, 500, 470);
//        this.setVisible(true);
//        this.setResizable(false);
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        btn = new JButton("浏览");// 钮1 
        btn.addActionListener(this);
        btn.setBackground(Color.red);
        
        constraints.gridwidth=GridBagConstraints.REMAINDER;
        JPanel p1 = new JPanel();
        p1.add(labe3);
        p1.add(textField3);
        p1.add(labe4);
        p1.add(textField4);
        p1.add(labe5);
        p1.add(textField5);
        p1.add(labe6);
        p1.add(textField6);
        p1.add(labe7);
        p1.add(textField7);  
        p1.add(label);
        p1.add(textField);
        p1.add(btn);  
        p1.setBorder(BorderFactory.createLineBorder(Color.yellow, 1));
        gbaglayout.setConstraints(p1, constraints);
        frame.add(p1);
        constraints.gridwidth=GridBagConstraints.REMAINDER;
        
        JPanel p2 = new JPanel();
        p2.add(labe2); 
        p2.add(jsp2); 
        btn2 = new JButton("制单导出");// 钮1
        btn2.addActionListener(this);
        btn2.setBackground(Color.red);
        p2.add(btn2);
        p2.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        gbaglayout.setConstraints(p2, constraints);
        frame.add(p2);
        constraints.gridwidth=GridBagConstraints.REMAINDER;
        
        JPanel p3 = new JPanel();
        p3.add(labe8); 
        p3.add(jsp8);
        constraints.gridwidth=GridBagConstraints.REMAINDER;
        box.setText("审批 单据模板是否导出");
        p3.add(box); 
        btn3 = new JButton("审批导出");// 钮1
        btn3.setBackground(Color.red);
        btn3.addActionListener(this);
        p3.add(btn3);  
        p3.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
        gbaglayout.setConstraints(p3, constraints);
        frame.add(p3);
        constraints.gridwidth=GridBagConstraints.REMAINDER;
        
        frame.setBounds(500,200,480,650);    //设置容器大小
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    @Override
	public void actionPerformed(ActionEvent actionevent) {
    	if(actionevent.getActionCommand().equals("浏览")){
    		JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.showDialog(new JLabel(), "选择");
            File file = chooser.getSelectedFile();
            textField.setText(file.getAbsoluteFile().toString()); 
    	}else{
    		String ip = textField3.getText();
			String port = textField4.getText();
			String orcl = textField5.getText();
			String username = textField6.getText();
			String password = textField7.getText();
			filePath = textField.getText(); 
			String funcode = textField2.getText(); //制单功能注册编码
			String aptradecode = textField8.getText();//审批交易类型编码
			boolean boxd = box.isSelected();
//			String ip = "202.108.31.142";
//			String port = "51521";
//			String orcl = "orcl";
//			String username = "hdapp2020";
//			String password = "hdapp2020";
//			filePath = "G:\\20200106-youzhi\\";
//			String funcode = "MUJ00700";
			if(null==ip || "".equals(ip.trim())){
				JOptionPane.showMessageDialog(this,"oracle数据库地址 不能空");
				return;
			}
			if(null==port || "".equals(port.trim())){
				JOptionPane.showMessageDialog(this,"oracle数据库端口 不能空");
				return;
			}
			if(null==orcl || "".equals(orcl.trim())){
				JOptionPane.showMessageDialog(this,"oracle数据库实例 不能空");
				return;
			}
			if(null==username || "".equals(username.trim())){
				JOptionPane.showMessageDialog(this,"oracle数据库用户 不能空");
				return;
			}
			if(null==password || "".equals(password.trim())){
				JOptionPane.showMessageDialog(this,"oracle数据库密码 不能空");
				return;
			}
			if(null==filePath || "".equals(filePath)){
				JOptionPane.showMessageDialog(this,"导出文件路径 不能空");
				return;
			}
			
			
			if(actionevent.getActionCommand().equals("制单导出")){
				pre = "";
				table =  new String[]{"sm_funcregister","sm_butnregister","sm_menuitemreg","pub_systemplate_base",
				    	"pub_billtemplet","pub_billtemplet_b","pub_billtemplet_t","muap_bill_h","muap_bill_b","muap_billyq_b"};//table数组 
	    		try { 
	    			if(null==funcode || "".equals(funcode.trim())){
	    				JOptionPane.showMessageDialog(this,"制单功能注册编码 不能同时空");
	    				return;
	    			}
	    			if(null!=ip && !"".equals(ip.trim()) && null!=port && !"".equals(port.trim()) && null!=orcl && !"".equals(orcl.trim())
	    					   							 && null!=username && !"".equals(username.trim()) && null!=password && !"".equals(password.trim())){
	    				exeOracle(ip.trim(),port.trim(),orcl.trim(),username.trim(),password.trim(),filePath.trim(),funcode.trim(), "制单",false);
	    			} 
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(this, e.getMessage());
					e.printStackTrace();
				}
	    	} 
			
			
			if(actionevent.getActionCommand().equals("审批导出")){
				pre = "approve-";
				table = new String[]{"muap_bill_h","muap_bill_b","muap_billyq_b","pub_systemplate_base","pub_billtemplet","pub_billtemplet_b","pub_billtemplet_t"};//table数组
	    		try { 
	    			if(null==aptradecode || "".equals(aptradecode.trim())){
	    				JOptionPane.showMessageDialog(this,"审批交易类型编码 不能同时空");
	    				return;
	    			}
	    			if(null!=ip && !"".equals(ip.trim()) && null!=port && !"".equals(port.trim()) && null!=orcl && !"".equals(orcl.trim())
	    					   							 && null!=username && !"".equals(username.trim()) && null!=password && !"".equals(password.trim())){
	    				exeOracle(ip.trim(),port.trim(),orcl.trim(),username.trim(),password.trim(),filePath.trim(),aptradecode.trim() ,"审批",boxd);
	    			} 
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(this, e.getMessage());
					e.printStackTrace();
				}
	    	} 
    	}
    	
	} 
    
    public void exeOracle(String ip,String port,String orcl,String username,String password,String filePath,String funcode, String type,boolean boxd) throws SQLException{
    	List<String> listSQL = new ArrayList<String>();
        connectSQL("oracle.jdbc.driver.OracleDriver", 
        		   "jdbc:oracle:thin:@"+ip+":"+port+":"+orcl+"", 
        		   username, 
        		   password);//连接数据库
        String[] funcodes = funcode.replace("，", ",").split(",");
        StringBuffer funcodestr = new StringBuffer();
        for(int i=0; i<funcodes.length; i++){
        	funcodestr.append("'").append(funcodes[i]).append("',");
        }
        if(funcodestr.length()>2){
        	/**
        	 * 创建查询语句
        	 */
        	listSQL = createSQL(funcodestr.substring(0,funcodestr.length()-1),type,boxd);
        } 
        /**
         * 执行sql并拼装 
         */
        executeSQL(conn, sm, listSQL);
    }
     
 
    /**
     * 拼装查询语句  
     */
    private static List<String> createSQL(String code,String type,boolean boxd) {
        List<String> listSQL = new ArrayList<String>();
        if(type.equals("制单")){
        	//功能注册
            listSQL.add("select * from sm_funcregister where funcode in ("+code+") and nvl(dr,0)=0 order by funcode");
            //功能注册子表按钮
            listSQL.add("select * from sm_butnregister where parent_id in (select cfunid from sm_funcregister where funcode in ("+code+") and nvl(dr,0)=0 ) and nvl(dr,0)=0");
            //菜单注册
            listSQL.add("select * from sm_menuitemreg where funcode in ("+code+") and nvl(dr,0)=0 order by funcode");
            //模板分配
            listSQL.add("select * from pub_systemplate_base where funnode in ("+code+") and nvl(dr,0)=0 order by funnode");
            //单据模板主表
            listSQL.add("select * from pub_billtemplet where pk_billtemplet in (select templateid from pub_systemplate_base where funnode in ("+code+") and nvl(dr,0)=0) and nvl(dr,0)=0");
            //单据模板子表
            listSQL.add("select * from pub_billtemplet_b where pk_billtemplet in (select templateid from pub_systemplate_base where funnode in ("+code+") and nvl(dr,0)=0) and nvl(dr,0)=0");
            //单据模板尾表
            listSQL.add("select * from pub_billtemplet_t where pk_billtemplet in (select templateid from pub_systemplate_base where funnode in ("+code+") and nvl(dr,0)=0) and nvl(dr,0)=0");
            //单据设置主表
            listSQL.add("select * from muap_bill_h where vmobilebilltype in ("+code+") and nvl(dr,0)=0 order by vmobilebilltype");
            //单据设置子表
            listSQL.add("select * from muap_bill_b where pk_billconfig_h in (select pk_billconfig_h from muap_bill_h where vmobilebilltype in ("+code+") and nvl(dr,0)=0) and nvl(dr,0)=0");
            //单据设置第2子表
            listSQL.add("select * from muap_billyq_b where pk_billconfig_h in (select pk_billconfig_h from muap_bill_h where vmobilebilltype in ("+code+") and nvl(dr,0)=0) and nvl(dr,0)=0");
        }
        if(type.equals("审批")){
        	//审批编码可以不填写，默认所有审批节点
        	if(null==code || "".equals(code)){
        		//单据设置主表
	            listSQL.add("select * from muap_bill_h where bmapprove='Y' and nvl(dr,0)=0 order by vmobilebilltype desc");
	            //单据设置子表
	            listSQL.add("select * from muap_bill_b where pk_billconfig_h in (select * from muap_bill_h where bmapprove='Y' and nvl(dr,0)=0) and nvl(dr,0)=0");
	            //单据设置第2子表
	            listSQL.add("select * from muap_billyq_b where pk_billconfig_h in (select * from muap_bill_h where bmapprove='Y' and nvl(dr,0)=0) and nvl(dr,0)=0");
	            //默认模板分配
	            listSQL.add("select pub_systemplate_base.*\n" +
							"from bd_billtype\n" + 
							"join pub_systemplate_base on pub_systemplate_base.funnode=bd_billtype.nodecode and nvl(pub_systemplate_base.dr,0)=0 and pub_systemplate_base.nodekey like '%app%'\n" + 
							"where nvl(bd_billtype.dr,0)=0\n" + 
							"and bd_billtype.pk_billtypecode in (select ltrim(rtrim(vmobilebilltype)) from muap_bill_h where bmapprove='Y' and nvl(dr,0)=0)");
	            //导出审批单据模板
	            if(boxd){ 
		            listSQL.add("select *\n" +
								"from pub_billtemplet\n" + 
								"where nvl(dr,0)=0\n" + 
								"and  pk_billtemplet in (select pub_systemplate_base.templateid\n" + 
								"                        from bd_billtype\n" + 
								"                        join pub_systemplate_base on pub_systemplate_base.funnode=bd_billtype.nodecode and nvl(pub_systemplate_base.dr,0)=0 and pub_systemplate_base.nodekey like '%app%'\n" + 
								"                        where nvl(bd_billtype.dr,0)=0\n" + 
								"                        and bd_billtype.pk_billtypecode in (select ltrim(rtrim(vmobilebilltype)) from muap_bill_h where bmapprove='Y' and nvl(dr,0)=0) )");
		            listSQL.add("select *\n" +
								"from pub_billtemplet_b\n" + 
								"where nvl(dr,0)=0\n" + 
								"and  pk_billtemplet in (select pub_systemplate_base.templateid\n" + 
								"                        from bd_billtype\n" + 
								"                        join pub_systemplate_base on pub_systemplate_base.funnode=bd_billtype.nodecode and nvl(pub_systemplate_base.dr,0)=0 and pub_systemplate_base.nodekey like '%app%'\n" + 
								"                        where nvl(bd_billtype.dr,0)=0\n" + 
								"                        and bd_billtype.pk_billtypecode in (select ltrim(rtrim(vmobilebilltype)) from muap_bill_h where bmapprove='Y' and nvl(dr,0)=0) )");
		            listSQL.add("select *\n" +
								"from pub_billtemplet_t\n" + 
								"where nvl(dr,0)=0\n" + 
								"and  pk_billtemplet in (select pub_systemplate_base.templateid\n" + 
								"                        from bd_billtype\n" + 
								"                        join pub_systemplate_base on pub_systemplate_base.funnode=bd_billtype.nodecode and nvl(pub_systemplate_base.dr,0)=0 and pub_systemplate_base.nodekey like '%app%'\n" + 
								"                        where nvl(bd_billtype.dr,0)=0\n" + 
								"                        and bd_billtype.pk_billtypecode in (select ltrim(rtrim(vmobilebilltype)) from muap_bill_h where bmapprove='Y' and nvl(dr,0)=0) )");
	            }else{
	            	//不导出审批单据模板
	            	listSQL.add("select * from pub_billtemplet where 1=2");
	            	listSQL.add("select * from pub_billtemplet_b where 1=2");
	            	listSQL.add("select * from pub_billtemplet_t where 1=2");
	            }
	            
        	}else{
	        	//单据设置主表
	            listSQL.add("select * from muap_bill_h where vmobilebilltype in ("+code+") and nvl(dr,0)=0 order by vmobilebilltype");
	            //单据设置子表
	            listSQL.add("select * from muap_bill_b where pk_billconfig_h in (select pk_billconfig_h from muap_bill_h where vmobilebilltype in ("+code+") and nvl(dr,0)=0) and nvl(dr,0)=0");
	            //单据设置第2子表
	            listSQL.add("select * from muap_billyq_b where pk_billconfig_h in (select pk_billconfig_h from muap_bill_h where vmobilebilltype in ("+code+") and nvl(dr,0)=0) and nvl(dr,0)=0");
	            //默认模板分配
	            listSQL.add("select pub_systemplate_base.*\n" +
							"from bd_billtype\n" + 
							"join pub_systemplate_base on pub_systemplate_base.funnode=bd_billtype.nodecode and nvl(pub_systemplate_base.dr,0)=0 and pub_systemplate_base.nodekey like '%app%'\n" + 
							"where nvl(bd_billtype.dr,0)=0\n" + 
							"and bd_billtype.pk_billtypecode in (select ltrim(rtrim(vmobilebilltype)) from muap_bill_h where bmapprove='Y' and vmobilebilltype in ("+code+") and nvl(dr,0)=0)");
	            //导出审批单据模板
	            if(boxd){ 
		            listSQL.add("select *\n" +
								"from pub_billtemplet\n" + 
								"where nvl(dr,0)=0\n" + 
								"and  pk_billtemplet in (select pub_systemplate_base.templateid\n" + 
								"                        from bd_billtype\n" + 
								"                        join pub_systemplate_base on pub_systemplate_base.funnode=bd_billtype.nodecode and nvl(pub_systemplate_base.dr,0)=0 and pub_systemplate_base.nodekey like '%app%'\n" + 
								"                        where nvl(bd_billtype.dr,0)=0\n" + 
								"                        and bd_billtype.pk_billtypecode in (select ltrim(rtrim(vmobilebilltype)) from muap_bill_h where bmapprove='Y' and vmobilebilltype in ("+code+") and nvl(dr,0)=0) )");
		            listSQL.add("select *\n" +
								"from pub_billtemplet_b\n" + 
								"where nvl(dr,0)=0\n" + 
								"and  pk_billtemplet in (select pub_systemplate_base.templateid\n" + 
								"                        from bd_billtype\n" + 
								"                        join pub_systemplate_base on pub_systemplate_base.funnode=bd_billtype.nodecode and nvl(pub_systemplate_base.dr,0)=0 and pub_systemplate_base.nodekey like '%app%'\n" + 
								"                        where nvl(bd_billtype.dr,0)=0\n" + 
								"                        and bd_billtype.pk_billtypecode in (select ltrim(rtrim(vmobilebilltype)) from muap_bill_h where bmapprove='Y' and vmobilebilltype in ("+code+") and nvl(dr,0)=0) )");
		            listSQL.add("select *\n" +
								"from pub_billtemplet_t\n" + 
								"where nvl(dr,0)=0\n" + 
								"and  pk_billtemplet in (select pub_systemplate_base.templateid\n" + 
								"                        from bd_billtype\n" + 
								"                        join pub_systemplate_base on pub_systemplate_base.funnode=bd_billtype.nodecode and nvl(pub_systemplate_base.dr,0)=0 and pub_systemplate_base.nodekey like '%app%'\n" + 
								"                        where nvl(bd_billtype.dr,0)=0\n" + 
								"                        and bd_billtype.pk_billtypecode in (select ltrim(rtrim(vmobilebilltype)) from muap_bill_h where bmapprove='Y' and vmobilebilltype in ("+code+") and nvl(dr,0)=0) )");
	            }else{
	            	//不导出审批单据模板
	            	listSQL.add("select * from pub_billtemplet where 1=2");
	            	listSQL.add("select * from pub_billtemplet_b where 1=2");
	            	listSQL.add("select * from pub_billtemplet_t where 1=2");
	            }
        	}
        }
        return listSQL;
    }
 
    
 
    /**
     * 执行sql并返回插入sql 
     */
    public void executeSQL(Connection conn, Statement sm, List listSQL) throws SQLException {
        List<String> insertSQL = new ArrayList<String>();
        ResultSet rs = null;
        try {
            rs = getColumnNameAndColumeValue(sm, listSQL, rs);
        } catch (SQLException e) {
        	JOptionPane.showMessageDialog(null,e.getMessage());
            e.printStackTrace();
        } finally {
            rs.close();
            sm.close();
            conn.close();
        }
    }
 
    /**
     * 获取列名和列值 
     */
    private ResultSet getColumnNameAndColumeValue(Statement sm, List listSQL, ResultSet rs) throws SQLException {
        if (listSQL.size() > 0) {
            for (int j = 0; j < listSQL.size(); j++) {
                String sql = String.valueOf(listSQL.get(j));
                rs = sm.executeQuery(sql);
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount(); 
                List<String> insertList = new ArrayList<>();// 存放insertsql文件的数据
                String rowsql = new String();
                while (rs.next()) { 
                	rowsql = new String();
                	StringBuffer ColumnName = new StringBuffer();
                    StringBuffer ColumnValue = new StringBuffer();
                    for (int i = 1; i <= columnCount; i++) {
                    	String value = "";
                    	if(null!=rs.getString(i) && !"".equals(rs.getString(i))){
                    		value = rs.getString(i).trim();
                    	} 
                        if (i == 1 || i == columnCount) {
                            if(i==columnCount){
                                ColumnName.append(",");
                            }
                            ColumnName.append(rsmd.getColumnName(i));
                            if( i== 1){
                                if (Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i) || Types.LONGVARCHAR == rsmd.getColumnType(i)) {
                                    ColumnValue.append("'").append(value).append("',");
                                } else if (Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i) || Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i) || Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i) || Types.DECIMAL == rsmd.getColumnType(i)|| Types.TINYINT == rsmd.getColumnType(i)) {
                                	if(null==value || "".equals(value)){
                                    	value = null;
                                    }
                                	ColumnValue.append(value).append(",");
                                } else if (Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i) || Types.TIMESTAMP == rsmd.getColumnType(i)) {
                                    ColumnValue.append("timestamp'").append(value).append("',");
                                } else {
                                    ColumnValue.append(value).append(",");
 
                                }
                            }else{
                                if (Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i) || Types.LONGVARCHAR == rsmd.getColumnType(i)) {
                                    ColumnValue.append("'").append(value).append("'");
                                } else if (Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i) || Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i) || Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i) || Types.DECIMAL == rsmd.getColumnType(i)|| Types.TINYINT == rsmd.getColumnType(i)) {
                                	if(null==value || "".equals(value)){
                                    	value = null;
                                    }
                                	ColumnValue.append(value);
                                } else if (Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i) || Types.TIMESTAMP == rsmd.getColumnType(i)) {
                                    ColumnValue.append("timestamp'").append(value);
                                } else {
                                    ColumnValue.append(value);
 
                                }
                            }
 
                        } else {
                            ColumnName.append("," + rsmd.getColumnName(i));
                            if (Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i) || Types.LONGVARCHAR == rsmd.getColumnType(i)) {
                                ColumnValue.append("'").append(value).append("'").append(",");
                            } else if (Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i) || Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i) || Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i) || Types.DECIMAL == rsmd.getColumnType(i)|| Types.TINYINT == rsmd.getColumnType(i)) {
                                if(null==value || "".equals(value)){
                                	value = null;
                                }
                            	ColumnValue.append(value).append(",");
                            } else if (Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i) || Types.TIMESTAMP == rsmd.getColumnType(i)) {
                                ColumnValue.append("timestamp'").append(value).append("',");
                            } else {
                                ColumnValue.append(value).append(",");
                            }
                        }
                    }
                    System.out.println(ColumnName.toString());
                    System.out.println(ColumnValue.toString());
                    rowsql = insertSQL(ColumnName, ColumnValue,table[j]);
                    insertList.add(rowsql);
                } 
                if(insertList.size()>0){
                	insertList.add("commit;");
                    createFile(insertList,table[j]);
                }
            }
            /**
             * 更新sql文件
             */
            List<String> updateList = new ArrayList<>();
            updateList.add("update muap_bill_h set pk_group=(select pk_group from org_group where nvl(dr,0)=0);");
            updateList.add("update sm_menuitemreg set pk_menu=(select pk_menu from sm_menuregister where nvl(dr,0)=0 and isenable='Y') where menuitemcode like 'HM%';");
            updateList.add("commit;");
            createFile(updateList,"Z-update");
            JOptionPane.showMessageDialog(null,"导出成功!");
        }
        return rs;
    }
    
 
    /**
     * 拼装insertsql放到全局list里面 
     */
    private static String insertSQL(StringBuffer ColumnName, StringBuffer ColumnValue,String tableName) {
        StringBuffer insertSQL = new StringBuffer();
        insertSQL.append(insert).append(" ")
                .append(tableName).append("(").append(ColumnName.toString()).append(")").append(values).append("(").append(ColumnValue.toString()).append(");");
        System.out.println(insertSQL.toString()); 
        return insertSQL.toString();
    } 
    
    
    /**
     * 创建insertsql.txt并导出数据
     */
    private void createFile(List<String> insertList,String tableName) {
        File file = new File(filePath+"\\"+pre+tableName+".sql");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            	JOptionPane.showMessageDialog(null,e.getMessage());
                System.out.println("创建文件名失败！！");
                e.printStackTrace();
            }
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            if (insertList.size() > 0) {
                for (int i = 0; i < insertList.size(); i++) {
                    bw.append(insertList.get(i));
                    bw.append("\n");
                }
            }
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(null,e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 连接数据库创建statement对象 
     */
    public static void connectSQL(String driver, String url, String UserName, String Password) {
        try {
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, UserName, Password);
            sm = conn.createStatement();
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(null,e.getMessage());
            e.printStackTrace();
        }
    }
}