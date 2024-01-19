#include<mpir.h>
#include <iostream>
#include <time.h>
#include <cstdlib>




using namespace std;

bool Solovei(mpz_t p) {
    srand(time(NULL));
    mpz_t p1;
    mpz_init(p1);
    mpz_sub_ui(p1, p, 1);
    mpz_div_ui(p1, p1, 2);
    int k = 5;
    mpz_t x,xpow;
    mpz_init(x);
    mpz_init(xpow);
    int x1 = 0;
    for (int i = 0; i < k; i++) {
        x1 = 0;
        while (x1 <= 1) x1 = rand();
        mpz_set_ui(x, x1);
        mpz_mod(x, x, p);
        mpz_t gcd;
        mpz_init(gcd);
        mpz_gcd(gcd,x, p);
        if (mpz_cmp_ui(gcd, 1) != 0)return 0;

        mpz_powm(xpow, x, p1, p);//x^((p-1)/2)
        if (mpz_cmp_ui(xpow, mpz_jacobi(x, p)) != 0)return 0;
    }
    return 1;
}

bool Ferma(mpz_t p) {
    srand(time(NULL));
    mpz_t p1;
    mpz_init(p1);
    mpz_sub_ui(p1, p, 1);
    int k = 5;
    mpz_t x, xpow;
    mpz_init(x);
    mpz_init(xpow);
    int x1 = 0;
    for (int i = 0; i < k; i++) {
        x1 = 0;
        while (x1 <= 1) x1 = rand();
        mpz_set_ui(x, x1);
        mpz_mod(x, x, p);
        mpz_t gcd;
        mpz_init(gcd);
        mpz_gcd(gcd, x, p);
        if (mpz_cmp_ui(gcd, 1) != 0)return 0;

        mpz_powm(xpow, x, p1, p);//x^((p-1))
        if (mpz_cmp_ui(xpow,1) != 0)return 0;
    }
    return 1;
}

bool Millera_rabina(mpz_t p) {
    int k = 5;//100 раундов
    mpz_t p1, t;
    mpz_init(p1);
    mpz_init(t);
    mpz_sub_ui(p1, p, 1);//p-1


    int s = 0;
    while (mpz_fdiv_ui(p1, 2) == 0) {
        s++;
        mpz_div_ui(p1, p1, 2);//после цикла p1=t
    }

    mpz_set(t, p1);
    mpz_sub_ui(p1, p, 1);

    for (int i = 0; i < k; i++) {
        mpz_t a, x;
        mpz_init(a);
        mpz_init(x);
        gmp_randstate_t state;
        gmp_randinit_default(state);
        gmp_randseed_ui(state, time(NULL));
        mpz_urandomm(a, state, p1);
        mpz_powm(x, a, t, p);
        if ((mpz_cmp_ui(x, 1) == 0) || (mpz_cmp(x, p1) == 0))continue;
        for (int j = 0; j < s - 1; j++) {
            mpz_powm_ui(x, x, 2, p);
            if (mpz_cmp_ui(x, 1) == 0)return 0;
            if (mpz_cmp(x, p1) == 0)continue;
        }
        return 0;
    }

    return 1;
}//якщо проcте то 1



void Byte_BBS(mpz_t generated, mpz_t p, mpz_t q, mpz_t n, mpz_t r) {
    mpz_set_ui(generated, 0);
    
    mpz_t one, two, x, two56;
    mpz_init(x);
    mpz_init_set_ui(two, 2);
    mpz_init_set_ui(one, 1);
    mpz_init_set_ui(two56, 256);
    for (int i = 0; i < 256;i++) {//256 byte generated
        mpz_mul_ui(generated,generated, 16);
        mpz_powm(r, r, two, n);
        mpz_mod(x, r, two56);
        mpz_add(generated, generated, x);
    }

}
void generate_prime(mpz_t prime, gmp_randstate_t rand_state, mpz_t max) {
    mpz_set_ui(prime, 9);
    while (Ferma(prime)==0) {
        mpz_urandomm(prime, rand_state, max);///k
        mpz_mul_ui(prime, prime, 4);//4k
        mpz_add_ui(prime, prime, 3);//4k+3
    }
}



int main()
{ 
   


    
    mpz_t p,q,n,r;
    mpz_init_set_str(r, "30086EC4", 16);
    mpz_init_set_str(p, "D5BBB96D30086EC484EBA3D7F9CAEB07", 16);
    mpz_init_set_str(q, "425D2B9BFDB25B9CF6C416CC6E37B59C1F", 16);
    mpz_init(n);
    mpz_mul(n, q, p);
    mpz_t generated;
    mpz_init(generated);
    Byte_BBS(generated, p, q, n,r);
    cout << "random seq: " << endl;
    cout << std::hex << generated << endl;;
    mpz_t prime, max;
    mpz_init(prime);
    mpz_init_set_ui(max, 2);//max=2
    mpz_pow_ui(max, max, 1024);//max=2^1024
    gmp_randstate_t state;
    gmp_randinit_default(state);
    gmp_randseed_ui(state, time(NULL));
    generate_prime(prime, state, max);
    cout << "prime:"<<endl;
    cout << std::hex << prime<<endl;

    cout<<mpz_probab_prime_p(prime, 10);
}
