package main

import "fmt"

var arr [10]float32

func main() {
    fill(10)
    dump(10)
    fmt.Println("all done")
}

func fill( k int ) {
    if k >= 0 {
        k -= 1
        arr[k] = float32(k*k)
        fill(k)
    }
}

func dump( k int ) {
    if (k > 0) {
        k -= 1;
        dump(k)
        fmt.Println("k, arr[k] =", k, arr[k])
    }
}