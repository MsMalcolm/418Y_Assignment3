//CODE TAKEN FROM NINAD: 
// - https://github.com/ninadpchaudhari/spring-boot-thymeleaf-aws-s3
// - https://github.com/ninadpchaudhari/spring-data

package com.example.accessingdatamysql;

import java.io.IOException;
import java.util.Optional;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

// import org.springframework.web.bind.annotation.RequestMapping;

@Controller	// This means that this class is a Controller
// @RequestMapping(path="/users") // This means URL's start with /demo (after Application path)
public class MainController {
	@Autowired // This means to get the bean called userRepository
			   // Which is auto-generated by Spring, we will use it to handle the data
	private UserRepository userRepository;
	
	@Autowired
	private PictureRepository pictureRepository;

	// @Value("#{environment.accesskey}")
	@Value("${accesskey}")
	String accesskey;
	@Value("${secretkey}")
	String secretkey;
	@Value("${bucketName}")
	String bucketName;

	// http://localhost:8080/users/add
	@PostMapping(path="/add") // Map ONLY POST Requests
	public @ResponseBody String addNewUser (
		@RequestParam String name
			, @RequestParam String email) {
		// @ResponseBody means the returned String is the response, not a view name
		// @RequestParam means it is a parameter from the GET or POST request

		User n = new User();
		n.setName(name);
		n.setEmail(email);
		userRepository.save(n);
		return "Saved";
	}

	@GetMapping(path="/all")
	public @ResponseBody Iterable<User> getAllUsers() {
		// This returns a JSON or XML with the users
		return userRepository.findAll();
	}
	@GetMapping(path="/user")
	public @ResponseBody Optional<User> getOneUser(@RequestParam Integer id) {
		// This returns a JSON or XML with the users
		return userRepository.findById(id);
	}

	@GetMapping(path="/userByName")
	public @ResponseBody User getOneUserByName(@RequestParam String name) {
		return userRepository.findByName(name);
	}
	@GetMapping(path="/addUser")
	public ModelAndView showPage(){
		return new ModelAndView("signupForm");
	}

	@GetMapping("/login")
	public String login(){
		return "login";
	}

	@GetMapping("/edit")
	public ModelAndView edit(){
		return new ModelAndView("edit");
	}

	@GetMapping("/home")
	public ModelAndView home(){
		return new ModelAndView("home");
	}

	@PostMapping(value = "/upload")
    public ModelAndView uploads3(@RequestParam("photo") MultipartFile image, @RequestParam(name = "desc") String desc) {
        ModelAndView returnPage = new ModelAndView();
        System.out.println("description      " + desc);
        System.out.println(image.getOriginalFilename());
    
        BasicAWSCredentials cred = new BasicAWSCredentials(accesskey, secretkey);
        // AmazonS3Client client=AmazonS3ClientBuilder.standard().withCredentials(new
        // AWSCredentialsProvider(cred)).with
        AmazonS3 client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(cred))
                .withRegion(Regions.US_EAST_1).build();
        try {
            PutObjectRequest put = new PutObjectRequest(bucketName, image.getOriginalFilename(),
                    image.getInputStream(), new ObjectMetadata()).withCannedAcl(CannedAccessControlList.PublicRead);
            client.putObject(put);

            String imgSrc = "http://" + bucketName + ".s3.amazonaws.com/" + image.getOriginalFilename();

            returnPage.setViewName("showImage");
            returnPage.addObject("name", desc);
            returnPage.addObject("imgSrc", imgSrc);

			ProfilePic p = new ProfilePic();
			p.setUrl(imgSrc);
			p.setName(desc);
			pictureRepository.save(p);
			
        } catch (IOException e) {
            e.printStackTrace();
            returnPage.setViewName("error");
        }
        return returnPage;
    }
}
