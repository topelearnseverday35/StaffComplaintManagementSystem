package FinalYearProject.StaffComplaintMgmtSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StaffComplaintMgmtSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(StaffComplaintMgmtSystemApplication.class, args);
	}

}
