func tmp(a int) {
	a = a + 100
	return a
}


func main() {
	var x int = 15
	var b int = 0
	b = tmp(x)
	write(b)
	
	return
}