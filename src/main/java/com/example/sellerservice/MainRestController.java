package com.example.sellerservice;

import com.netflix.discovery.converters.Auto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class MainRestController
{
    Logger logger = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    CredentialRepository credentialRepository;
    @Autowired
    UserdetailRepository userdetailRepository;
    @Autowired
    UsertypelinkRepository usertypelinkRepository;
    @Autowired
    OfferDetailRepository offerDetailRepository;
    @Autowired
    ProductofferstatusRepository productofferstatusRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderstatusRepository orderstatusRepository;
    @Autowired
    UserService userService;
    @Autowired
    ComposeOrderService composeOrderService;
    @Autowired
    ProductOfferService productOfferService;
    @Autowired
    private WebClient.Builder webClientBuilder;

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
    @PostMapping("save/offer")
    public ResponseEntity<String> saveOffer(@RequestBody OfferDetail offer)
    {
        offerDetailRepository.save(offer);
        Productofferstatus productofferstatus = new Productofferstatus();
        productofferstatus.setOfferid(offer.getId());
        productofferstatus.setStatus("OPEN");
        productofferstatus.setId(String.valueOf(((int) (Math.random()*10000))));
        productofferstatusRepository.save(productofferstatus);
        return new ResponseEntity<>("New Offer Created",HttpStatus.OK);
    }

    @GetMapping("get/offer/all")
    public List<ProductOffer> getAllOffers()
    {
        List<OfferDetail> offerDetailList = offerDetailRepository.findAll();

        return offerDetailList.stream().map((offerDetail) -> {

            return productOfferService.getOffer(offerDetail.getId());

        }).collect(Collectors.toList());

    }

    @GetMapping("get/order/sellerwise/{sellername}")
    public List<ComposedOrder> getOrdersSellerwise(@PathVariable String sellername)
    {
        List<Order> orderList = orderRepository.findAll();
        return orderList.stream().filter(order -> productOfferService.
                        getOffer(order.getOfferid()).getOfferDetail().
                        getSellername().equals(sellername)).
                map(order -> {

                    return composeOrderService.composeOrder(order.getOfferid(), order.getBuyername(), order);

                } ).collect(Collectors.toList());
    }

    @PostMapping("save/order/status")
    public ResponseEntity<String> setOrderStatus(@RequestBody Orderstatus orderstatus)
    {
        orderstatusRepository.save(orderstatus);
        String data = "?orderid="+orderstatus.getOrderid()+"&offerid="+orderRepository.findById(orderstatus.getOrderid()).get().getOfferid();

        Payment payment = new Payment();
        payment.setId(String.valueOf((int)(Math.random()*1000000)));
        payment.setOrderid(orderstatus.getOrderid());
        payment.setOfferid(orderRepository.findById(orderstatus.getOrderid()).get().getOfferid());
        payment.setStatus("DUE");

        if(orderstatus.getStatus().equals("ACCEPTED"))
        {
            logger.info("request forwarded to payment  service");
            Mono<String> response =  webClientBuilder.build().post().uri("http://localhost:8072/payment-service/save/payment").
                    body(Mono.just(payment),Payment.class).retrieve().bodyToMono(String.class);

            return new ResponseEntity<>(response.toString(),HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity<>("Order Rejected",HttpStatus.OK);
        }
        //return new ResponseEntity<>("Order Status Updated", HttpStatus.OK);
    }
}
