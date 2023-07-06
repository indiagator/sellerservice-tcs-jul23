package com.example.sellerservice;

import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
public class MainRestController
{
    @Autowired
    CredentialRepository credentialRepository;
    @Autowired
    UserdetailRepository userdetailRepository;
    @Autowired
    UsertypelinkRepository usertypelinkRepository;
    @Autowired
    UserService userService;

    @PostMapping("save/seller/details")
    public ResponseEntity<String> saveUserDetails(@RequestParam("username") String username,
                                                  @RequestParam("fname") String fname,
                                                  @RequestParam("lname") String lname,
                                                  @RequestParam("email") String email,
                                                  @RequestParam("phone") String phone)
    {
        Userdetail userdetail = new Userdetail();
        userdetail.setId(username);
        userdetail.setFname(fname);
        userdetail.setLname(lname);
        userdetail.setEmail(email);
        userdetail.setPhone(phone);
        userdetailRepository.save(userdetail);

        Usertypelink usertypelink = new Usertypelink();
        usertypelink.setId(String.valueOf(Integer.valueOf((int) (Math.random()*10000))));
        usertypelink.setUsername(username);
        usertypelink.setType("SELLER");
        usertypelinkRepository.save(usertypelink);

        return new ResponseEntity<>("Userdetails Updated", HttpStatus.OK);
    }

    @GetMapping("/get/seller/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username)
    {
        Optional<List<Usertypelink>> usertypelinkList =  usertypelinkRepository.findByUsername(username);

        if(usertypelinkList.isPresent())
        {
            if (usertypelinkList.get().stream().filter(usertypelink -> usertypelink.getType().equals("SELLER")).findAny().isPresent())
            {
                return new  ResponseEntity<>(userService.getUser(username),HttpStatus.OK) ;
            }
            else
            {
                User user = null;
                return new  ResponseEntity<>(user,HttpStatus.NO_CONTENT);
            }
        }
        else
        {
            User user = null;
            return new  ResponseEntity<>(user,HttpStatus.NO_CONTENT) ;
        }

    }
}
