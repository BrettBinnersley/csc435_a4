package main

import "fmt"

func main() {
	sum := 0
	for sum < 10 {
        fmt.Println(sum)
		sum = sum + 1
	}
	fmt.Println(sum)
}
