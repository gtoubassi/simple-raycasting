# Simple Ray Casting renderer

This repo represents a simple, Java based ray casting implementation similar
to those used in early "3D" games like Wolfenstein 3D.

You can toggle between textured and "flat" (gray) walls with the 'T' key.

![raycasting recording](https://github.com/gtoubassi/simple-raycasting/blob/master/raycastdemo.gif?raw=true)

### Speed

Being in Java is a bit silly but it turns out the actual ray casting is still
plenty fast clocking in at ~6500 fps for flat walls and ~2500 fps for
textured on my 1.7Ghz core i7 Macbook Air.  Even the textured should be fast
enough to run on hardware from the Wolfenstein 3D era.

I do have a huge tax in Java due to garbage collection though given that the
ray caster doesn't actually turn over garbage I assume by choosing the right
GC algorithm maybe this can be resolved.  In order to get clean measurements
on the actual ray casting I run GC on every tick before going into the loop
to ensure no GC disturbs the ray casting.

I also have some tax (not quantified) to update the display.  In a real game
you'd get closer to direct vram access.  I'm doing the best I could find
with Java.

### Fisheye Distortion

I had three causes of distortion in my initial implementation.  The first is
that as you sweep through the "field of view" angles, 1 per vertical "strip"
of the display, I was sweeping with a fixed angle, but that means the
plane of the camera was actually being sampled in a non uniform way.  Really
you want to sample at non uniform angles such that you intersect the camera
plane in uniform intersects.  That's the cause of the Math.atan call.

The second cause of distortion was that you want to take the distance not
from the viewpoint of the camera, but to the plane of the camera.  This
is the cause of the cosine adjustment after the ray distance is computed.

The third distortion I had was dumb.  When I got the distance for a given
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

When the player gets too close to a wall and the wall intersects the plane of
the camera it distorts (flattens) against the camera plane.  Its not readily
obvious to me if this is a bug or if I just need to tune everything so the
camera plane and field of view and how close you can get to a wall
conveniently avoids the issue.

### Ideas

It occurs to me that really for many simple maps there is no reason to cast
all rays, but instead find two vertical strips (hopefully far apart on screen)
that hit the same wall, and if so you can do linear interpolation on all
intermediate strips.  For example the map could identify each wall with a
unique identifier and return that from the hit testing (Map.isWall).

Consider the simple case where the user is looking at a single wall that
covers the entire field of view.  Do the ray casting on the left most strip
and find the distance.  Do it on the right most strip, find the distance, and
do linear interpolation in between.  If the wall IDs don't match bisect
and try again.  This would only work if you can make the assumption that if
wall X is hit at strip 0 (far left), and wall X is hit at strip 319 (far
right), then no other obstruction (say a freestanding cube) is occluding view
in between.

This could lead to a further optimization which could allow you to avoid the
vertical rendering of strips, which due to the Screen.WIDTH stride means you
have potentially worse cache behavior with how we are painting vram.  In other
words you'd rather paint the screen in memory order (left to right, top to
bottom) vs a vertical line at a time.  You'd take the wall boundaries found in
the bisection pass and turn it into a list of trapezoids to be renders.  A
single visible wall would be a single trapezoid which can then be rendered
right to left.  If you are looking at a corner it would be two trapezoids.

### References

* http://www.cokeandcode.com/info/tut2d.html
* https://lodev.org/cgtutor/raycasting.html
