package com.backend;

import com.backend.rest.auth.AuthenticationService;
import com.backend.rest.auth.dto.RegisterRequest;
import com.backend.rest.topic.Topic;
import com.backend.rest.topic.TopicRepository;
import com.backend.rest.topic.TopicService;
import com.backend.rest.user.Role;
import com.backend.rest.user.User;
import com.backend.rest.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
		System.out.println("BackendApplication started");
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			TopicService topicService,
			AuthenticationService authenticationService
	) {
		return args -> {

			String[] fruits = {"Orange", "Banana", "Mango", "Kiwi"};
			String[] nationalFlag = {"VietNam", "Germany", "Mexico", "Japan", "Canada", "Australia", "Brazil"};

			Topic fruitTopic = Topic
					.builder()
					.name("Fruits")
					.illustrationUrl("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.mooringspark.org%2Fnews%2Fhealth-benefits-of-your-favorite-fruits&psig=AOvVaw0oD6-yS89dSV1uf6PZDYZc&ust=1714707009233000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCOCVvcWD7oUDFQAAAAAdAAAAABAE")
					.note("Lorem ipsum dolor sit amet, consectetur adipiscing elit")
					.words(fruits)
					.build();

			Topic nationalFlagTopic = Topic
					.builder()
					.name("National Flag")
					.illustrationUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/2/21/Flag_of_Vietnam.svg/2560px-Flag_of_Vietnam.svg.png")
					.note("Aenean at diam in urna ullamcorper pulvinar")
					.words(nationalFlag)
					.build();

			topicService.save(fruitTopic);
			topicService.save(nationalFlagTopic);
			System.out.println("Generated TOPIC Data");

			var userA = RegisterRequest.builder()
					.username("minh_quan")
					.password("quan2003")
					.displayName("Đỗ Mai Minh Quân")
					.role(Role.USER)
					.build();

			var userB = RegisterRequest.builder()
					.username("trong_ninh")
					.password("ninh2003")
					.displayName("Nguyễn Trọng Ninh")
					.role(Role.USER)
					.build();

			var userC = RegisterRequest.builder()
					.username("ngoc_tin")
					.password("tin2003")
					.displayName("Nguyễn Ngọc Tín")
					.role(Role.USER)
					.build();

			System.out.println("Quan token: " + authenticationService.register(userA).getAccessToken());
			System.out.println("Ninh token: " + authenticationService.register(userB).getAccessToken());
			System.out.println("Tinn token: " + authenticationService.register(userC).getAccessToken());

			System.out.println("Swagger: : http://localhost:8081/swagger-ui/index.html#/");
		};
	};
}
