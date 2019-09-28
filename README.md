
TEAM- Blitzkrieg

Name- Akash Paul (https://www.hackerearth.com/@Blitzen)[https://www.hackerearth.com/@Blitzen]

Problem statement-  Intelligent Parcel shipping/Tracking/Receiving using AI/ML-based techniques Visual recognition of parcel type and suggestive packing to reduce shipping cost and space.

___

# JUST. PACK. IT.

### This is an android app which lets the user measure their parcel & suggest them the best fitting & cheapest box for shipping. Thereby saving the shipper packaging costs & enabling Pitney Bowes to earn & ship more packages due to optimized packaging in the same crate/container.

## User-flow

* The app asks the user to place the object on any flat surface

* Then the app guides the user to rotate the object along its length, width & height & lets them measure the distance along each side by selecting the start & end vertices

* Once all three sides are done the app calculates the volume & suggests the most suitable box from the listed boxes at https://www.pitneybowes.us/shop/ink-and-supplies/shipping-and-packing-supplies/mailing-boxes--cartons/en-us/storeus 

* Accepting the recommended box takes the user to the checkout page


---

Beneficiary/User segment-

* **The shipper of the parcel**

How it helps the beneficiary/user segment- 

* **Allows them to choose the best option for shipping their parcel**

The impact that the solution would create; the impact metrics that one can use to analyze the effect of the solution-

* **Pitney Bowes can measure the weight of a crate/container filled with non-optimised packaging & optimised packaging over a short period of time & determine how much more value per crate/container this optimisation delivers**

Time to make a prototype-

* **A full day's time**

Frameworks/Technologies and APIs to be used-

* **ARCore for Android, Android Studio, Linear programming library for optimal solution of volume/box from given dimension constraints**

Assumptions, Constraints-

* **Parcel is a box, or I could actually ask the user is it is a box-item or a document & suggest an alternative type of packaging**

Effectiveness of the solution in solving the problem defined and ease of implementation-

* **The presence of a ruler is obviously something that might render measuring using the ARCore's ML techniques useless, but coupled with the ease of simply tapping points on a screen & also finding the volume of box needed from available inventory is something best left to the app instead of back-of-the-napkin linear programming solutions for countless parcels throughout the day**

