func main() {
	var x int
	var z int = 0
	var y int = 1000
	x = 979
	
	for x > 0 {
		if x > 100 {
			write (x)
		} else {
			write (y)
		}
		x = x / 2
		z = z + x
	}
	
	write(z)
}