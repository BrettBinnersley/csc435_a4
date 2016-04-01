extern int printf(const char*, ...);

float arr[10];
void fill(int);
void dump(int);

// declared as int to eliminate clang's warning message
int main() {
    fill(10);
    dump(10);
    printf("%s \n", "all done");
    return 0;
}

void fill( int k ) {
    if (k >= 0) {
        k -= 1;
        arr[k] = (float)(k*k);
        fill(k);
    }
}

void dump( int k ) {
    if (k > 0) {
        k -= 1;
        dump(k);
        printf("%s %d %g \n", "k, arr[k] =", k, arr[k]);
    }
}