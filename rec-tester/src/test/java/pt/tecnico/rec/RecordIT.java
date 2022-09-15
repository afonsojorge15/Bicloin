package pt.tecnico.rec;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static io.grpc.Status.INVALID_ARGUMENT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.grpc.StatusRuntimeException;

public class RecordIT extends BaseIT {
	
	// static members
	private static final String EMPTY_ID = "";
	private static final String INVALID_ID = "   ";
	private static final String OVERSIZE_ID = "Longbottom";
	private static final String VALID_ID = "friend";


	// one-time initialization and clean-up
	@BeforeAll
	public static void oneTimeSetUp(){
		
	}
	
	@AfterAll
	public static void oneTimeTearDown() {
		
	}
	
	// initialization and clean-up for each test
	
	@BeforeEach
	public void setUp() {
		
	}
	
	@AfterEach
	public void tearDown() {
		
	}
		
	// tests 
	
	@Test
	public void test() {
		
		
	}
}
