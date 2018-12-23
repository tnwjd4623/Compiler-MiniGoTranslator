var arr[10] int

func main() {
	set_arr()
	show_arr()
	return
}

func set_arr() {
	var i = 0
	for i=0; i<10; i++ {
		if i % 2 == 0 {
			arr[i] = i * 5
		} else {
			arr[i] = i
		}
	}
	return
}

func show_arr() {
	var i = 0
	for i=9; i>=0; i-- {
		write (i)
		write (arr[i])
		lf()
	}
	return
}