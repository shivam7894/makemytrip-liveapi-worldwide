package com.mmt.controller;
import com.mmt.model.*; import com.mmt.repository.*; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/reviews") @CrossOrigin(origins="*")
public class ReviewController {
 private final ReviewRepository reviews; private final ReviewReplyRepository replies;
 public ReviewController(ReviewRepository r,ReviewReplyRepository rp){reviews=r;replies=rp;}
 @GetMapping public List<Review> list(@RequestParam String entityType,@RequestParam Long entityId,@RequestParam(defaultValue="newest") String sort){
   List<Review> x=new ArrayList<>(reviews.findByEntityTypeAndEntityIdAndModeratedRemovedFalseOrderByCreatedAtDesc(entityType,entityId));
   x.forEach(r->r.setReplies(replies.findByReviewIdOrderByCreatedAtAsc(r.getId())));
   if("highest".equals(sort))x.sort(Comparator.comparing(Review::getRating).reversed());
   if("helpful".equals(sort))x.sort(Comparator.comparing(Review::getHelpfulCount,Comparator.nullsFirst(Integer::compareTo)).reversed()); return x;
 }
 @GetMapping("/hotel/{hotelId}") public List<Review> hotelReviews(@PathVariable Long hotelId){ return list("HOTEL",hotelId,"newest"); }
 @PostMapping public Review create(@RequestBody Map<String,Object> b){
   Review r=Review.builder().userId(Long.valueOf(b.get("userId").toString()))
     .entityType(b.getOrDefault("entityType","HOTEL").toString())
     .entityId(Long.valueOf((b.containsKey("entityId")?b.get("entityId"):b.get("hotelId")).toString()))
     .rating(Integer.valueOf(b.get("rating").toString()))
     .text(b.containsKey("text")?b.get("text").toString():b.getOrDefault("comment","").toString())
     .photoUrl(b.containsKey("photoUrl")?b.get("photoUrl").toString():null)
     .createdAt(LocalDateTime.now()).flagged(false).moderatedRemoved(false).helpfulCount(0).build();
   return reviews.save(r);
 }
 @PostMapping("/{id}/helpful") public Review helpful(@PathVariable Long id){Review r=reviews.findById(id).orElseThrow();r.setHelpfulCount((r.getHelpfulCount()==null?0:r.getHelpfulCount())+1);return reviews.save(r);}
 @PostMapping("/{id}/flag") public Review flag(@PathVariable Long id){Review r=reviews.findById(id).orElseThrow();r.setFlagged(true);return reviews.save(r);}
 @PostMapping("/{id}/reply") public ReviewReply reply(@PathVariable Long id,@RequestBody Map<String,Object> b){String text=b.containsKey("text")?b.get("text").toString():b.getOrDefault("comment","").toString(); ReviewReply r=ReviewReply.builder().reviewId(id).userId(Long.valueOf(b.get("userId").toString())).text(text).createdAt(LocalDateTime.now()).build();return replies.save(r);}
 @GetMapping("/{id}/replies") public List<ReviewReply> replies(@PathVariable Long id){return replies.findByReviewIdOrderByCreatedAtAsc(id);}
 @GetMapping("/moderation") public List<Review> moderation(){return reviews.findAll().stream().filter(Review::isFlagged).toList();}
 @PostMapping("/moderation/{id}/remove") public Review remove(@PathVariable Long id){Review r=reviews.findById(id).orElseThrow();r.setModeratedRemoved(true);return reviews.save(r);}
}
