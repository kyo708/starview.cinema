# USER STORY AND USE CASE FOR CINEMA BOOKING SYSTEM PROJECT

## 1: Email 

> Hey guys,
> 
> Kevin from StarView Cinemas here. We're bleeding money because we can't sell tickets online. 
> People check showtimes on Google and then go to our competitors because they can book seats there.
> 
> We need a site where people can see what movies we're playing, watch the trailer, and pick their seats.
> The seat picking part is crucial—it has to look like a real theater layout.
> Once they pick, they pay (just mock this part), and get a confirmation.
> 
> My staff needs to be able to add new movies and schedule them into different rooms.
> 
> One big worry I have is double booking. What if two people try to buy the same seat at the same exact second?
> You guys handle that, right?
> 
> Also, to be competitive, I'm thinking about dynamic pricing—like tickets are cheaper on Tuesday mornings and more expensive on Friday nights.
> Can the system handle that logic? And maybe QR codes for tickets so we can just scan them at the door?
> 
> Thanks, Kevin

---

## 2: User story

| As a(an) | I want | So that |
| :--- | :--- | :--- |
| Admin | Establish dynamic pricing rule (ex: cheaper on Tuesday mornings, more expensive on Friday evenings) | Increase revenue and attract customers. |
| Staff | Add / edit / delete movie information | Update the latest blockbusters for theaters. |
| Staff | Schedule movie screenings in different rooms | Optimize theater capacity |
| Customer | See the list of currently showing movie and watch trailers | Decide which film want to see |
| Customer | Choose seat using the Visual Seat Map | Get the best viewing position |
| Customer | Make an online payment and receive instant booking confirmation (mock) | Convenient for payment |
| Customer | Receive a QR code after successfully booking a ticket | Quickly scan it at the entrance |

---

## 3: Use case: Online booking

* **Use case name:** Booking and payment
* **Actor:** Customer (Primary), payment system (supporting)

| Step | Actor Action | System Response |
| :--- | :--- | :--- |
| 1 | Selects a movie and a specific showtime. | Displays the Visual Seat Map for the selected theater room. |
| 2 | Picks available seats from the layout. | Calculates Ticket Price based on dynamic logic (e.g., Tuesday discounts or Friday night surcharges). |
| 3 | Clicks "Confirm Selection" to proceed. | Temporarily Locks (Holds) the selected seats for 5-10 minutes to prevent double-booking. |
| 4 | Enters payment details (Mock System). | Validates information and sends a request to the Mock Payment Gateway. |
| 5 | Completes the payment. | Updates seat status from "On Hold" to "Booked" and records the transaction. |
| 6 | Receives the booking result. | Generates a Unique QR Code and sends a confirmation email/notification to the Customer. |