package rediswing;


/**
 * 核心错误 1-10000
 * @author liuyufei
 *
 */
public class CoreException extends RuntimeException
{
	private static final long serialVersionUID = 5381325428017496758L;
	private String errorDetail ="NONE";

	/** 系统错误 未知错误 库错误 JAVA错误*/
	public static final int SYSTEM_INTERNAL_ERROR= 1;

	/** 系统错误 config配置初始化错误*/
	public static final int SYSTEM_CONFIG_INIT_ERROR = 5;
	/** 系统错误 用户不存在*/
	public static final int SYSTEM_USER_NOT_EXISTS = 6;
	/** 系统错误 用户未登录*/
	public static final int SYSTEM_USER_NOT_LOGIN = 7;
	/** 系统错误 加锁失败*/
	public static final int SYSTEM_LOCK_FAILED = 8;
	/** 系统错误 Redis错误*/
	public static final int SYSTEM_REDIS_ERROR = 9;
	/** 系统错误 Mysql错误*/
	public static final int SYSTEM_MYSQL_ERROR = 10;


    
	public CoreException(int errorCode){
		super(Integer.toString(errorCode));
	}

	public CoreException(int errorCode , String errorDetail){
		super(Integer.toString(errorCode));
		this.errorDetail = errorDetail;
	}
	public String getErrorDetail() {
		return errorDetail;
	}
}
