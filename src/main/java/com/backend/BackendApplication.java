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

			String[] fruits = {"Orange", "Banana", "Mango", "Kiwi", "Apple", "Grapes", "Pineapple", "Strawberry", "Watermelon", "Peach"};
			String[] nationalFlag = {"VietNam", "Germany", "Mexico", "Japan", "Canada", "Australia", "Brazil"};
			String[] animals = {"Dog", "Cat", "Mouse", "Chicken", "Cow", "Pig", "Goat", "Duck", "Rabbit", "Horse", "Parrot"};

			String[] programmingLanguages = {
					"Java",
					"Python",
					"C++",
					"JavaScript",
					"C#",
					"PHP",
					"Ruby",
					"Swift",
					"Go",
					"Kotlin"
			};

			String[] anime = {
					"Naruto",
					"One Piece",
					"Dragon Ball",
					"Attack on Titan",
					"My Hero Academia",
					"Death Note",
					"Fullmetal Alchemist",
					"Sword Art Online",
					"Demon Slayer",
					"Tokyo Ghoul"
			};

			String[] pokemon = {
					"Pikachu",
					"Charizard",
					"Bulbasaur",
					"Squirtle",
					"Jigglypuff",
					"Meowth",
					"Eevee",
					"Mewtwo",
					"Snorlax",
					"Gengar"
			};


			Topic fruitTopic = Topic
					.builder()
					.name("Fruits")
					.illustrationUrl("https://www.mooringspark.org/hs-fs/hubfs/bigstock-Fresh-Fruits-assorted-Fruits-C-365480089%20Large.jpeg")
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

			Topic animalTopic = Topic
					.builder()
					.name("Animals")
					.illustrationUrl("https://media.newyorker.com/photos/62c4511e47222e61f46c2daa/4:3/w_2663,h_1997,c_limit/shouts-animals-watch-baby-hemingway.jpg")
					.note("Aliquam varius faucibus erat, quis consequat odio semper eu")
					.words(animals)
					.build();

			Topic programmingLanguagesTopic = Topic
					.builder()
					.name("Programming Languages")
					.illustrationUrl("https://i.pinimg.com/originals/8e/23/1e/8e231e0aa5c7acb23e299ae2f4889fbe.png")
					.note("Ut fringilla non quam vel lacinia")
					.words(programmingLanguages)
					.build();

			Topic animeTopic = Topic
					.builder()
					.name("Anime")
					.illustrationUrl("https://i.ytimg.com/vi/xXmXM0qRMbo/hq720.jpg")
					.note("Phasellus elementum dignissim dignissim")
					.words(anime)
					.build();

			Topic pokemonTopic = Topic
					.builder()
					.name("Pokemon")
					.illustrationUrl("https://yt3.googleusercontent.com/wzEypbVsmY9BI-IbLwVius4UvC2rejtJB_PTXAdPpYXQ07EIjl5Ms55NCFq_dILwONpxrzE2xA=s900-c-k-c0x00ffffff-no-rj")
					.note("Praesent ac mi eleifend, consectetur lorem imperdiet, tincidunt purus")
					.words(pokemon)
					.build();

			topicService.save(fruitTopic);
			topicService.save(nationalFlagTopic);
			topicService.save(animalTopic);
			topicService.save(pokemonTopic);
			topicService.save(programmingLanguagesTopic);
			topicService.save(animeTopic);
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
