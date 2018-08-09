# Simple Ray Casting renderer

This repo represents a simple, Java based ray casting implementation similar
to those used in early "3D" games like Wolfenstein 3D.

### Speed

Being in Java is a bit silly but it turns out the actual ray casting is still
plenty fast clocking in at ~6500 fps on my 1.7Ghz core i7 Macbook Air.
This easily should have been able to run 60 fps on hardware of that era
(386 @33Mhz).

I do have a huge tax in Java due to garbage collection though given that the
ray caster doesn't actually turn over garbage I assume by choosing the right
GC algorithm maybe this can be resolved.  In order to get clean measurements
on the actual ray tracing I run GC on every tick before going into the loop
to ensure no GC disturbs the ray tracing.

I also have some tax (not quantified) to update the display.  In a real game
you'd get closer to direct vram access.  I'm doing the best I could find
on Java.

### Fisheye Distortion

I had two causes of distortion in my initial implementation.  The first is
that as you sweep through the "field of view" angles, 1 per vertical "strip"
of the display, I was sweeping with a fixed angle, but that means the
plane of the camera was actually being samples in a non uniform way.  Really
you want to sample at non uniform angles such that you intersect the camera
plane in uniform intersects.  Thats the cause of the Math.atan call.

The bigger distortion I had was dumb.  When I got the distance for a given
strip, I just took that distance, divided by two, and used that as the
margin at the top and bottom of the scan line.  For example if a strip was
20 pixels away, I'd draw the vertical line for the wall with a margin at
the top and bottom of 10 pixels each.  If it was further away, say 50, I'd
use a 25px margin top and bottom.  This creates a horrible distortion and
the right thing to do is a true perspective warp where you take the height
of the wall (which is always fixed in code because all walls are the same
height) and divide by the distance.  So 1/d vs 1-d/2.  This hyperbolic
transform corrected the fisheye effect.

### Known Bugs

I'm not doing an actual DDA to intersect rays with walls. I'm just marching
1 unit per step (in the direction that is changing fastest) so I may actually
detect an intersection "late".  Thus when you get close to the wall you can
see some jaggies at the top and bottom.  I really should be marching the ray
and calculating intersections at both the vertical and horizontal grid lines.


### References

* http://www.cokeandcode.com/info/tut2d.html
* https://lodev.org/cgtutor/raycasting.html
